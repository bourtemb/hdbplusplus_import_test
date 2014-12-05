
include ../../Make-hdb++.in

CXXFLAGS += -Wall -DRELEASE='"$HeadURL: $ "' \
		-I$(CASSANDRA_INC) -I$(TANGO_INC) -I$(OMNI_INC) -I$(LIBHDB_INC)
CXX = g++


##############################################
# support for shared libray versioning
#
LFLAGS_SONAME = $(CASSANDRA_LIB) -Wl,-soname,
SHLDFLAGS = -shared
BASELIBNAME       =  libhdb++cassandra
SHLIB_SUFFIX = so

#  release numbers for libraries
#
 LIBVERSION    = 0
 LIBRELEASE    = 0
 LIBSUBRELEASE = 1
#

LIBRARY       = $(BASELIBNAME).a
DT_SONAME     = $(BASELIBNAME).$(SHLIB_SUFFIX).$(LIBVERSION)
DT_SHLIB      = $(BASELIBNAME).$(SHLIB_SUFFIX).$(LIBVERSION).$(LIBRELEASE).$(LIBSUBRELEASE)
SHLIB         = $(BASELIBNAME).$(SHLIB_SUFFIX)



.PHONY : install clean

lib/LibHdb++cassandra: lib obj obj/LibHdb++Cassandra.o
	$(CXX) obj/LibHdb++Cassandra.o $(SHLDFLAGS) $(LFLAGS_SONAME)$(DT_SONAME) -o lib/$(DT_SHLIB)
	ln -sf $(DT_SHLIB) lib/$(SHLIB)
	ln -sf $(SHLIB) lib/$(DT_SONAME)
	ar rcs lib/$(LIBRARY) obj/LibHdb++Cassandra.o

obj/LibHdb++Cassandra.o: src/LibHdb++Cassandra.cpp src/LibHdb++Cassandra.h $(LIBHDB_INC)/LibHdb++.h
	$(CXX) $(CXXFLAGS) -fPIC -c src/LibHdb++Cassandra.cpp -o $@

clean:
	rm -f obj/*.o lib/*.so* lib/*.a

lib obj:
	@mkdir $@
	
	