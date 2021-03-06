
Jargon Core API


# Project: Jargon-core API
#### Date: 
#### Release Version: 4.0.2.1-SNAPSHOT
#### git tag: MASTER
#### Developer: Mike Conway - DICE

## News

work on milestone: https://github.com/DICE-UNC/jargon/issues?milestone=4&state=open

=======

Please go to [[https://github.com/DICE-UNC/jargon]] for the latest news and info.

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-user-profile - allows management of user profile and related configuration data in a user home directory
* jargon-conveyor - transfer manager for managing and synchronizing data with iRODS
* jargon-ticket - support for ticket processing
* jargon-httpstream - stream http content into iRODS via Jargon
* jargon-ruleservice - support for running and managing rules from interfaces
* jargon-workflow - support for iRODS workflows

## Requirements

*Jargon depends on Java 1.7+
*Jargon is built using Apache Maven2, see POM for dependencies
*Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.0.3 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Bug Fixes

#### fix display version of file length for rounding #54

Fix display of file size to not round down for CollectionAndDataObjectListingEntry

#### Overwriting a file with IRODSFileOutputStream deletes file metadata #52

IRODSOutputStream now has extended support for open flags, including truncation/overwrite handling.  In the immediate case this prevents deletion of AVUs on 
overwrite of a file via OutputStream.  Extended operations are available 

READ, WRITE, READ_WRITE, READ_TRUNCATE, WRITE_TRUNCATE, READ_WRITE_CREATE_IF_NOT_EXISTS, WRITE_FAIL_IF_EXISTS, READ_WRITE_FAIL_IF_EXISTS

#### #62 [iROD-Chat:12888] JargonException: java.io.IOException: read length is set to zero when copying file

Clarified the usage of the default storage resource, if set, in IRODSAccount so that it is propagated to move() and copy() operations if a specific resource was not set in the move or copy call.
Note that if no resource is set, it will defer to default resource settings in iRODS.  Additionally, the move() method was enhanced, so that, if a move is being done from a source file to the same target
file, but with a different resource, it will delegate to a physical move.  This seems like a 'least surprise' sensible default.

#### #68 display of sharing when group assigned does not show up in shared w me 

Updated specific query to not select user, and added view of 'public' in sharing listings.  This is in the jargon-user-tagging specific queries

####  appending path in dataObjectAOImpl causes duplicate file name #73

Fix path munging in removeAccessPermissionsInAdminMode in DataObjectAOImpl

## Features

#### Setting inheritance on collection as admin #55

Add ability to set inherit/noinherit in CollectionAO as admin

#### Add create/mod date/data size to DataObjectAOImpl.findMetadataValuesByMetadataQuery #60

Include additional information on data size, create and modify dates to MetaDataAndDomainData,
useful in metadata query based virtual collections

#### Connection tester #67

Add connection tester package to jargon-data-utils to test out and measure put/get performance 

#### Parallel file transfer performance #72

Additional tweaks to improve parallel file transfer performance

