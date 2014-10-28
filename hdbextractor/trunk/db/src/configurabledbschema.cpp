#include "configurabledbschema.h"
#include "dbschemaprivate.h"

/** \brief An abstract implementation of the DbSchema interface.
 * The class offers an implementation of the DbSchema interface adding the
 * management of an object called QueryConfiguration, whose aim is storing
 * useful configuration parameters to be passed to the hdb extractor.
 *
 * Subclass ConfigurableDbSchema to implement DbSchema and provide a
 * QueryConfiguration for your database schema related methods.
 *
 * @see MySqlHdbppSchema
 * @see MySqlHdbSchema
 */
ConfigurableDbSchema::ConfigurableDbSchema()
{
    d_ptr = new DbSchemaPrivate();
}

/** \brief Personalize queries.
 *
 * @param queryConfiguration a QueryConfiguration object.
 *
 */
void ConfigurableDbSchema::setQueryConfiguration(QueryConfiguration *queryConfiguration)
{
    d_ptr->queryConfiguration = queryConfiguration;
}

