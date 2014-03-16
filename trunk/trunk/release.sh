# /bin/bash

######## user configurable parameters ########
EXEC_JAR=gen/mPutF.jar
LIB_JAR=gen/mPutF-bindings.jar
DOC_JAR=gen/mPutF-javadoc.jar
DOC_DIR=gen/doc

MAJOR_VERSION=1


######## do not edit below this line ########
TMP_JOB_NAME=$JOB_NAME
TARGET_DIR=/var/www/release/${TMP_JOB_NAME// /_}

TARGET_EXEC_JAR=$(basename "$EXEC_JAR")
TARGET_EXEC_JAR=${TARGET_EXEC_JAR%.*}-$MAJOR_VERSION.$BUILD_NUMBER.${TARGET_EXEC_JAR##*.}
TARGET_LIB_JAR=$(basename "$LIB_JAR")
TARGET_LIB_JAR=${TARGET_LIB_JAR%.*}-$MAJOR_VERSION.$BUILD_NUMBER.${TARGET_LIB_JAR##*.}
TARGET_DOC_JAR=$(basename "$DOC_JAR")
TARGET_DOC_JAR=${TARGET_DOC_JAR%.*}-$MAJOR_VERSION.$BUILD_NUMBER.${TARGET_DOC_JAR##*.}
TARGET_DOC_DIR=$TARGET_DIR/doc-$MAJOR_VERSION.$BUILD_NUMBER

if [ -n "$DEBUG" ]
then
	echo "Jenkins Environment Variables..."
	echo "JOB_NAME:     '$JOB_NAME'"
	echo "BUILD_NUMBER: '$BUILD_NUMBER'"
	echo "WORKSPACE:    '$WORKSPACE'"
	echo
	echo "User Variables..."
	echo "EXEC_JAR: '$EXEC_JAR'"
	echo "LIB_JAR: '$LIB_JAR'"
	echo "DOC_JAR: '$DOC_JAR'"
	echo "DOC_DIR: '$DOC_DIR'"
	echo "MAJOR_VERSION: '$MAJOR_VERSION'"
	echo "TARGET_DIR: '$TARGET_DIR'"
	echo
	echo "Derived Variables..."
	echo "TARGET_EXEC_JAR: '$TARGET_EXEC_JAR'"
	echo "TARGET_LIB_JAR: '$TARGET_LIB_JAR'"
	echo "TARGET_DOC_JAR: '$TARGET_DOC_JAR'"
	echo 
	echo "Commands..."
	echo "cp $WORKSPACE/$EXEC_JAR $TARGET_DIR/$TARGET_EXEC_JAR"
	echo "cp $WORKSPACE/$LIB_JAR $TARGET_DIR/$TARGET_LIB_JAR"
	echo "cp $WORKSPACE/$DOC_JAR $TARGET_DIR/$TARGET_DOC_JAR"
	echo "cp -r $WORKSPACE/$DOC_DIR $TARGET_DOC_DIR"
else
	#execute the copy
	echo "Target dir: '$TARGET_DIR'"
	mkdir -p $TARGET_DIR/
	cp $WORKSPACE/$EXEC_JAR $TARGET_DIR/$TARGET_EXEC_JAR
	cp $WORKSPACE/$LIB_JAR $TARGET_DIR/$TARGET_LIB_JAR
	cp $WORKSPACE/$DOC_JAR $TARGET_DIR/$TARGET_DOC_JAR
	cp -r $WORKSPACE/$DOC_DIR $TARGET_DOC_DIR
fi

