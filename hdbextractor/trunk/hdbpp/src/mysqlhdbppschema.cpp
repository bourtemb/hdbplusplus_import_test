#include "mysqlhdbppschema.h"

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/time.h>
#include "db/src/connection.h"
#include "db/src/row.h"
#include "db/src/result.h"
#include "mysql/src/mysqlconnection.h"
#include "../src/hdbxmacros.h"
#include "db/src/dbschemaprivate.h"
#include "helpers/configurabledbschemahelper.h"
#include "xvariantlist.h"
#include "timeinterval.h"
#include <assert.h>
#include <map>

#define MAXQUERYLEN 4096
#define MAXTABLENAMELEN 32
#define MAXTIMESTAMPLEN 64

MySqlHdbppSchema::MySqlHdbppSchema(ResultListener *resultListenerI) : ConfigurableDbSchema()
{
    assert(resultListenerI != NULL);

    /* d_ptr is created inside ConfigurableDbSchema */
    d_ptr->resultListenerI = resultListenerI;
    d_ptr->variantList = NULL;
    d_ptr->sourceStep = 1;
    d_ptr->totalSources = 1;
    pthread_mutex_init(&d_ptr->mutex, NULL);
}

const char *MySqlHdbppSchema::getError() const
{
    return d_ptr->errorMessage;
}

bool MySqlHdbppSchema::hasError() const
{
    return strlen(d_ptr->errorMessage) > 0;
}

/** \brief The class destructor.
 *
 * Deallocates the mutex used for thread safety.
 */
MySqlHdbppSchema::~MySqlHdbppSchema()
{
    pthread_mutex_destroy(&d_ptr->mutex);
}

/** \brief empties the queue of partial or complete data already fetched from the database.
 *
 * @param variantlist a <strong>reference</strong> to a std::vector where data is copied.
 *
 * \note The caller is not in charge of freeing any memory used by MySqlHdbppSchema. The caller
 *       creates and manages the variantlist.
 */
int MySqlHdbppSchema::get(std::vector<XVariant>& variantlist)
{
    pthread_mutex_lock(&d_ptr->mutex);

    int size = -1;
    if(d_ptr->variantList != NULL)
    {
        size = (int) d_ptr->variantList->size();

        printf("\e[0;35mMySqlHdbppSchema.get: locketh xvarlist for writing... size %d \e[0m\t", size);

        for(int i = 0; i < size; i++)
        {
            //            printf("copying variant %d over %d\n", i, size);
            variantlist.push_back(XVariant(*(d_ptr->variantList->get(i))));
            printf("last timestamp %s\n", variantlist.at(variantlist.size() - 1).getTimestamp());
        }
        delete d_ptr->variantList;
        d_ptr->variantList = NULL;
    }

    pthread_mutex_unlock(&d_ptr->mutex);
    printf("\e[0;32munlocked: [copied %d]\e[0m\n", size);
    return size;
}

bool MySqlHdbppSchema::getData(const char *source,
                                const TimeInterval *time_interval,
                                Connection *connection,
                                int notifyEveryRows)
{
    return getData(source, time_interval->start(), time_interval->stop(), connection, notifyEveryRows);
}

bool MySqlHdbppSchema::getData(const std::vector<std::string> sources,
                                const TimeInterval *time_interval,
                                Connection *connection,
                                int notifyEveryRows)
{
    return getData(sources, time_interval->start(), time_interval->stop(), connection, notifyEveryRows);
}

/** \brief Fetch attribute data from the MySql hdb++ database between a start and stop date/time.
 *
 * Fetch data from the  MySql hdb++ database.
 *
 * \note This method is used by HdbExtractor and it is not meant to be directly used by the library user.
 *
 * @param source A the tango attribute in the form domain/family/member/AttributeName
 * @param start_date the start date (begin of the requested data interval) as string, such as "2014-07-10 10:00:00"
 * @param stop_date the stop date (end of the requested data interval) as string, such as "2014-07-10 12:00:00"
 * @param connection the database Connection specific object
 * @param notifyEveryRows the number of rows that make up a block of data. Every time a block of data is complete
 *        notifications are sent to the listener of type ResultListener (HdbExtractor)
 *
 * @return true if the call was successful, false otherwise.
 */
bool MySqlHdbppSchema::getData(const char *source,
                               const char *start_date,
                               const char *stop_date,
                               Connection *connection,
                               int notifyEveryNumRows)
{
    bool success;
    bool from_the_past_success = true;
    char query[MAXQUERYLEN];
    char errmsg[256];
    char ch_id[16];
    char data_type[32];
    char timestamp[32];
    char table_name[32];
    int id, datasiz = 1;
    int timestampCnt = 0;
    int index = 0;
    double elapsed = -1.0; /* query elapsed time in seconds.microseconds */
    double from_the_past_elapsed = 0.0; /* fetch from the past query time */
    struct timeval tv1, tv2;

    XVariant *xvar = NULL;
    strcpy(timestamp, ""); /* initialize an empty timestamp */

    gettimeofday(&tv1, NULL);

    /* clear error */
    strcpy(d_ptr->errorMessage, "");

    d_ptr->notifyEveryNumRows = notifyEveryNumRows;

    snprintf(query, MAXQUERYLEN, "SELECT att_conf_id,data_type from att_conf WHERE att_name like '%%/%s'", source);
    Result * res = connection->query(query);
    if(!res)
    {
        snprintf(d_ptr->errorMessage, MAXERRORLEN,
                 "MySqlHdbppSchema.getData: error in query \"%s\": \"%s\"", query, connection->getError());
        return false;
    }
    if(res->next() > 0)
    {
        Row* row = res->getCurrentRow();
        if(!row)
        {
            snprintf(d_ptr->errorMessage, MAXERRORLEN, "MySqlHdbppSchema.getData: error getting row");
            return false;
        }

        if(row->getFieldCount() == 2)
        {
            strncpy(ch_id, row->getField(0), 16);

            strncpy(data_type, row->getField(1), 32);

            /*
             * enum AttributeDataType { ATT_BOOL, ATT_SHORT, ATT_LONG, ATT_LONG64, ATT_FLOAT,
             * ATT_DOUBLE, ATT_UCHAR, ATT_USHORT, ATT_ULONG, ATT_ULONG64, ATT_STRING,
             * ATT_STATE, DEVICE_STATE,
             * ATT_ENCODED, NO_DATA ...
             */
            XVariant::DataType dataType;
            if(strstr(data_type, "double") != NULL)
                dataType = XVariant::Double;
            else if(strstr(data_type, "int64") != NULL)
                dataType = XVariant::Int;
            else if(strstr(data_type, "int8") != NULL)
                dataType = XVariant::Int;
            else if(strstr(data_type, "string") != NULL)
                dataType = XVariant::String;
            else if(strstr(data_type, "bool") != NULL)
                dataType = XVariant::Boolean;
            else
                dataType = XVariant::TypeInvalid;

            /* free memory */
            res->close();
            row->close();

            XVariant::Writable wri;
            if(strstr(data_type, "ro") != NULL)
                wri = XVariant::RO;
            else if(strstr(data_type, "rw") != NULL)
                wri = XVariant::RW;
            else if(strstr(data_type, "wo") != NULL)
                wri = XVariant::WO;
            else
                wri = XVariant::WritableInvalid;

            XVariant::DataFormat format;
            if(strstr(data_type, "scalar") != NULL)
                format = XVariant::Scalar;
            else if(strstr(data_type, "array") != NULL)
                format = XVariant::Vector;
            else if(strstr(data_type, "image") != NULL)
                format = XVariant::Matrix;
            else
                format = XVariant::FormatInvalid;

            if(dataType == XVariant::TypeInvalid || wri ==  XVariant::WritableInvalid ||
                    format == XVariant::FormatInvalid)
            {
                snprintf(d_ptr->errorMessage, MAXERRORLEN,
                         "MySqlHdbppSchema.getData: invalid type %d, format %d or writable %d",
                         dataType, format, wri);
                success = false;
            }
            else
            {
                const ConfigurableDbSchemaHelper *configHelper = new ConfigurableDbSchemaHelper();
                ConfigurableDbSchemaHelper::FillFromThePastMode fillMode = ConfigurableDbSchemaHelper::None;

                /* now get data */
                id = atoi(ch_id);
                if(wri == XVariant::RO)
                {
                    snprintf(table_name, MAXTABLENAMELEN, "att_%s", data_type);
                    if(format == XVariant::Vector)
                        snprintf(query, MAXQUERYLEN, "SELECT event_time,value_r,dim_x,idx FROM "
                                                     " %s WHERE att_conf_id=%d AND event_time >='%s' "
                                                     " AND event_time <= '%s' ORDER BY event_time,idx ASC",
                                 table_name, id, start_date, stop_date);
                    else if(format == XVariant::Scalar)
                        snprintf(query, MAXQUERYLEN, "SELECT event_time,value_r FROM "
                                                     " %s WHERE att_conf_id=%d AND event_time >='%s' "
                                                     " AND event_time <= '%s' ORDER BY event_time ASC",
                                 table_name, id, start_date, stop_date);
                    
                    printf("\e[1;32mquery: %s\e[0m\n", query);


                    res = connection->query(query);
                    if(!res)
                    {
                        snprintf(d_ptr->errorMessage, MAXERRORLEN, "error in query \"%s\": \"%s\"",
                                 query, connection->getError());
                        return false;
                    }

                    while(res->next() > 0)
                    {
                        row = res->getCurrentRow();

                        if(!row)
                        {
                            snprintf(d_ptr->errorMessage, MAXERRORLEN, "MySqlHdbppSchema.getData: error getting row");
                            return false;
                        }

                        /* compare timestamp with previous one: if they differ, the row
                         * refers to the next value in time.
                         */
                        if(strcmp(timestamp, row->getField(0)) != 0)
                        {
                            if(format != XVariant::Scalar && timestampCnt > 0 &&
                                    d_ptr->notifyEveryNumRows > 0 &&
                                    (timestampCnt % d_ptr->notifyEveryNumRows == 0))
                            {
                                printf("\e[1;33mnotrifying vector!!\e[0m\n");
                                d_ptr->resultListenerI->onProgressUpdate(source,
                                                                         timestampCnt,
                                                                         res->getRowCount() / datasiz);
                            }

                            /* get timestamp */
                            strncpy(timestamp, row->getField(0), 32);

                            if(timestampCnt == 0)
                            {
                                fillMode = configHelper->fillFromThePastMode(d_ptr->queryConfiguration,
                                                                             start_date, stop_date, timestamp);
                                if(fillMode != ConfigurableDbSchemaHelper::None)
                                {
                                    printf("RO: calling fetchInThePast\n");
                                    from_the_past_success = fetchInThePast(source, start_date, table_name, id,
                                                                           dataType, format, wri, connection,
                                                                           &from_the_past_elapsed, fillMode);
                                }
                            }

                            /* get data size of array */
                            if(format == XVariant::Vector)
                                datasiz = atoi(row->getField(2));
                            else
                                datasiz = 1;

                            /* create new XVariant for the timestamp */
                            xvar = new XVariant(source, timestamp, datasiz, format, dataType, wri);

                            timestampCnt++;

                            printf("+ xvar 0x%p: new source %s %s %s arr.cnt: %d data siz: %d entries cnt: %d)\n", xvar,
                                   source, row->getField(0), row->getField(1),
                                   timestampCnt, datasiz, res->getRowCount()/datasiz);

                            pthread_mutex_lock(&d_ptr->mutex);

                            if(d_ptr->variantList == NULL)
                                d_ptr->variantList = new XVariantList();
                            if(format == XVariant::Scalar)
                            {
                                xvar->add(row->getField(1), 0);
                            }
                            d_ptr->variantList->add(xvar);

                            pthread_mutex_unlock(&d_ptr->mutex);

                            if(format == XVariant::Scalar && timestampCnt > 0 &&
                                    d_ptr->notifyEveryNumRows > 0 &&
                                    (timestampCnt % d_ptr->notifyEveryNumRows == 0))
                            {
                                d_ptr->resultListenerI->onProgressUpdate(source,
                                                                         timestampCnt,
                                                                         res->getRowCount() / datasiz);
                            }
                        }

                        if(format == XVariant::Vector)
                        {
                            index = atoi(row->getField(3));
                            pthread_mutex_lock(&d_ptr->mutex);
                            xvar->add(row->getField(1), index);
                            pthread_mutex_unlock(&d_ptr->mutex);
                        }
                        row->close();

                    } /* end while(res->next) res is closed after else wri == XVariant::RW */

                    success = from_the_past_success;

                }  /* end else if(wri == XVariant::RO) */
                else if(wri == XVariant::RW)
                {
                    /*  */
                    snprintf(table_name, MAXTABLENAMELEN, "att_%s", data_type);
                    if(format == XVariant::Vector)
                        snprintf(query, MAXQUERYLEN, "SELECT event_time,value_r,value_w,dim_x,idx FROM "
                                                     " %s WHERE att_conf_id=%d AND event_time >='%s' "
                                                     " AND event_time <= '%s' ORDER BY event_time,idx ASC",
                                 table_name, id, start_date, stop_date);
                    else
                        snprintf(query, MAXQUERYLEN, "SELECT event_time,value_r,value_w FROM "
                                                     " %s WHERE att_conf_id=%d AND  event_time >='%s' "
                                                     " AND event_time <= '%s' ORDER BY event_time ASC",
                                 table_name, id, start_date, stop_date);

                    printf("\e[1;32mquery: %s\e[0m\n", query);

                    res = connection->query(query);
                    if(!res)
                    {
                        snprintf(d_ptr->errorMessage, MAXERRORLEN, "error in query \"%s\": \"%s\"",
                                 query, connection->getError());
                        return false;
                    }
                    while(res->next() > 0)
                    {
                        row = res->getCurrentRow();
                        if(!row)
                        {
                            snprintf(d_ptr->errorMessage, MAXERRORLEN, "MySqlHdbppSchema.getData: error getting row");
                            return false;
                        }

                        XVariant *xvar = NULL;

                        /* compare timestamp with previous one: if they differ, the row
                         * refers to the next value in time.
                         */
                        if(strcmp(timestamp, row->getField(0)) != 0)
                        {
                            if(timestampCnt > 0 &&
                                    d_ptr->notifyEveryNumRows > 0 &&
                                    (timestampCnt % d_ptr->notifyEveryNumRows == 0))
                            {
                                d_ptr->resultListenerI->onProgressUpdate(source, timestampCnt,
                                                                         res->getRowCount() / datasiz);
                            }
                            /* get timestamp */
                            strncpy(timestamp, row->getField(0), 32);

                            if(timestampCnt == 0)
                            {
                                fillMode = configHelper->fillFromThePastMode(d_ptr->queryConfiguration,
                                                                             start_date,
                                                                             stop_date,
                                                                             timestamp);
                                if(fillMode != ConfigurableDbSchemaHelper::None)
                                {
                                    /* must fetch the first data ahead of the start_date, create a
                                     * XVariant and insert it as first element, according to the
                                     * fillMode
                                     */
                                    printf("RW: calling fetchInThePast\n");
                                    from_the_past_success = fetchInThePast(source, start_date, table_name, id,
                                                                           dataType, format, wri, connection,
                                                                           &from_the_past_elapsed,
                                                                           fillMode);
                                }
                            }

                            /* get data size of array */
                            if(format == XVariant::Vector)
                                datasiz = atoi(row->getField(3));
                            else
                                datasiz = 1;

                            /* create new XVariant for the timestamp */
                            xvar = new XVariant(source, timestamp, datasiz, format, dataType, wri);
                            /*  this means a new array is being associated to a timestamp */
                            timestampCnt++;

                            pthread_mutex_lock(&d_ptr->mutex);
                            if(d_ptr->variantList == NULL)
                                d_ptr->variantList = new XVariantList();

                            d_ptr->variantList->add(xvar);
                            pthread_mutex_unlock(&d_ptr->mutex);
                        }

                        if(format == XVariant::Vector)
                            index = atoi(row->getField(4));
                        else
                            index = 0; /* scalar */

                        pthread_mutex_lock(&d_ptr->mutex);
                        xvar->add(row->getField(1), row->getField(2), index);
                        pthread_mutex_unlock(&d_ptr->mutex);

                        row->close();
                    } /* end while(res->next() > 0) */

                    /* res->close() is called after RW else */
                    success = from_the_past_success;
                } /* else if(wri == XVariant::RW) */

                if(res->getRowCount() == 0)
                {
                    fillMode = configHelper->fillFromThePastMode(d_ptr->queryConfiguration,
                                                                 start_date, stop_date, timestamp);
                    if(fillMode != ConfigurableDbSchemaHelper::None)
                    {
                        printf("RO: calling fetchInThePast\n");
                        from_the_past_success = fetchInThePast(source, start_date, table_name, id,
                                                               dataType, format, wri, connection,
                                                               &from_the_past_elapsed, fillMode);
                        if(from_the_past_success)
                           timestampCnt++;
                    }
                }

                res->close();

                delete configHelper;

            } /* else: valid data type, format, writable */

            if(timestampCnt > 0 &&
                    d_ptr->notifyEveryNumRows > 0)
            {
                d_ptr->resultListenerI->onProgressUpdate(source,
                                                         res->getRowCount() / datasiz,
                                                         res->getRowCount() / datasiz);
            }
        }
    }
    else
    {
        success = false;
        snprintf(d_ptr->errorMessage, MAXERRORLEN, "MySqlHdbppSchema: no attribute \"%s\" in adt", source);
        perr(errmsg);
    }

    /* compute elapsed time */
    gettimeofday(&tv2, NULL);
    /* transform the elapsed time from a timeval struct to a double whose integer part
     * represents seconds and the decimal microseconds.
     */
    elapsed = tv2.tv_sec + 1e-6 * tv2.tv_usec - (tv1.tv_sec + 1e-6 * tv1.tv_usec) + from_the_past_elapsed;
    /* sourceStep is managed by the const std::vector<std::string> input version of getData */
    d_ptr->resultListenerI->onFinished(source, d_ptr->sourceStep, d_ptr->totalSources, elapsed);

    return success;
}

bool MySqlHdbppSchema::getData(const std::vector<std::string> sources,
                               const char *start_date,
                               const char *stop_date,
                               Connection *connection,
                               int notifyEveryNumRows)
{
    bool success = true;
    d_ptr->totalSources = sources.size();
    for(size_t i = 0; i < d_ptr->totalSources; i++)
    {
        d_ptr->sourceStep = i + 1;
        printf("MySqlHdbppSchema.getData %s %s %s\n", sources.at(i).c_str(), start_date, stop_date);
        success = getData(sources.at(i).c_str(), start_date, stop_date,
                          connection, notifyEveryNumRows);
        if(!success)
            break;
    }

    d_ptr->totalSources = 1;
    d_ptr->sourceStep = 1;
    return success;

}

bool MySqlHdbppSchema::getSourcesList(Connection *connection, std::list<std::string> result) const
{
    return findSource(connection, "", result);
}

bool MySqlHdbppSchema::findSource(Connection *connection, const char *substring, std::list<std::string> result) const
{

    bool success = true;
    char query[MAXQUERYLEN];
    Row *row;

    snprintf(query, MAXQUERYLEN, "SELECT att_name from att_conf WHERE att_name like '%%%s%%'", substring);

    Result * res = connection->query(query);
    if(!res)
    {
        snprintf(d_ptr->errorMessage, MAXERRORLEN,
                 "MysqlHdbSchema.getSourcesList: error in query \"%s\": \"%s\"", query, connection->getError());
        success = false;
    }
    else
    {
        while(res->next() > 0 && success)
        {
            row = res->getCurrentRow();
            if(!row)
            {
                snprintf(d_ptr->errorMessage, MAXERRORLEN, "MysqlHdbSchema.getSourcesList: error getting row");
                success = false;
            }
            else
            {
                result.push_back(std::string(row->getField(0)));
            }
        }
    }
    return success;
}

bool MySqlHdbppSchema::fetchInThePast(const char *source,
                                      const char *start_date, const char *table_name,
                                      const int att_id,
                                      XVariant::DataType dataType,
                                      XVariant::DataFormat format,
                                      XVariant::Writable writable,
                                      Connection *connection,
                                      double *time_elapsed,
                                      ConfigurableDbSchemaHelper::FillFromThePastMode mode)
{
    char query[MAXQUERYLEN];
    char timestamp[MAXTIMESTAMPLEN];
    int datasiz = 1;
    int index = 0;
    struct timeval tv1, tv2;
    Result *res = NULL;
    Row *row = NULL;

    gettimeofday(&tv1, NULL);

    if(format != XVariant::Scalar)
    {
        snprintf(query, MAXQUERYLEN, "SELECT event_time FROM %s WHERE att_conf_id=%d AND "
             " event_time <= '%s' ORDER BY event_time DESC LIMIT 1", table_name, att_id, start_date);

        printf("\e[1;32mquery: %s\e[0m\n", query);
        res = connection->query(query);
        if(!res)
        {
            snprintf(d_ptr->errorMessage, MAXERRORLEN, "MySqlHdbppSchema.fetchInThePast: bad query \"%s\": \"%s\"",
                     query, connection->getError());
            return false;
        }
        else if(res->getRowCount() == 1)
        {
            while(res->next() > 0)
            {
                row = res->getCurrentRow();
                if(!row)
                {
                    snprintf(d_ptr->errorMessage, MAXERRORLEN, "MySqlHdbppSchema.fetchInThePast: error getting row");
                    return false;
                }
                else
                        strncpy(timestamp, row->getField(0), MAXTIMESTAMPLEN);

                row->close();
            }
            res->close();
        }
        else if(res->getRowCount() == 0)
        {
            pinfo("MySqlHdbppSchema.fetchInThePast: no data before \"%s\"", start_date);
            res->close();
            return true; /* no error actually */
        }
    } /* if format not scalar */

    printf("\e[1;4;35mfetching in the past \"%s\" before %s\e[0m\n", source, start_date);
    if(writable == XVariant::RO && format != XVariant::Scalar)
    {
        snprintf(query, MAXQUERYLEN, "SELECT event_time,dim_x,idx,value_r FROM "
                                     " %s WHERE att_conf_id=%d AND event_time = "
                                     " '%s' ORDER BY idx ASC",
                 table_name, att_id, timestamp);
    }
    else if(writable == XVariant::RO)
    {
        snprintf(query, MAXQUERYLEN, "SELECT event_time, 1 AS dim_x, 0 AS idx,value_r FROM "
                                     " %s WHERE att_conf_id=%d AND event_time <= "
                                     " '%s' ORDER BY event_time DESC LIMIT 1",
                 table_name, att_id, start_date);
    }
    else if(writable == XVariant::RW && format != XVariant::Scalar)
    {
        snprintf(query, MAXQUERYLEN, "SELECT event_time,dim_x,idx,value_r,value_w FROM "
                                     " %s WHERE att_conf_id=%d AND event_time = "
                                     " '%s' ORDER BY idx ASC",
                 table_name, att_id, start_date);
    }
    else if(writable == XVariant::RW)
    {
        snprintf(query, MAXQUERYLEN, "SELECT event_time, 1 AS dim_x, 0 AS idx,value_r,value_w FROM "
                                     " %s WHERE att_conf_id=%d AND event_time  <= "
                                     " '%s' ORDER BY event_time DESC LIMIT 1",
                 table_name, att_id, start_date);
    }

    printf("\e[1;32mquery: %s\e[0m\n", query);
    res = connection->query(query);
     if(!res)
    {
        snprintf(d_ptr->errorMessage, MAXERRORLEN, "MySqlHdbppSchema.fetchInThePast: bad query \"%s\": \"%s\"",
                 query, connection->getError());
        return false;
    }

    XVariant *xvar = NULL;

    while(res->next() > 0)
    {
        row = res->getCurrentRow();
        if(!row)
        {
            snprintf(d_ptr->errorMessage, MAXERRORLEN, "MySqlHdbppSchema.fetchInThePast: error getting row");
            return false;
        }
        else
        {
            /* get data size of array */
            datasiz = atoi(row->getField(1));
            index = atoi(row->getField(2));

            /* create new XVariant */
            if(!xvar)
            {
                if(mode == ConfigurableDbSchemaHelper::KeepWindow)
                    strncpy(timestamp, start_date, MAXTIMESTAMPLEN);
                else
                    strncpy(timestamp, row->getField(0), MAXTIMESTAMPLEN);

                /* If format is spectrum, we create the xvar the first time and then
                     * we simply add data from the following rows (else below).
                     * Otherwise, we'll never enter the else below.
                     */
                xvar = new XVariant(source, timestamp, datasiz, format, dataType, writable);

                pthread_mutex_lock(&d_ptr->mutex);
                if(d_ptr->variantList == NULL)
                    d_ptr->variantList = new XVariantList();
                d_ptr->variantList->add(xvar);
                if(writable == XVariant::RW)
                    xvar->add(row->getField(3), row->getField(4), index);
                else
                    xvar->add(row->getField(3), index);

                pthread_mutex_unlock(&d_ptr->mutex);
            }
            else
            {
                pthread_mutex_lock(&d_ptr->mutex);
                if(writable == XVariant::RW)
                    xvar->add(row->getField(3), row->getField(4), index);
                else
                    xvar->add(row->getField(3), index);
                pthread_mutex_unlock(&d_ptr->mutex);
            }
        }
    }

    if(time_elapsed)
    {
        /* compute elapsed time */
        gettimeofday(&tv2, NULL);
        /* transform the elapsed time from a timeval struct to a double whose integer part
         * represents seconds and the decimal microseconds.
         */
        *time_elapsed = tv2.tv_sec + 1e-6 * tv2.tv_usec - (tv1.tv_sec + 1e-6 * tv1.tv_usec);
    }
    return true;
}
