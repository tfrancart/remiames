export CLASSIFICATION_OUTPUT_FILE="output/classification-output.ttl"
export MERGE_OUTPUT_FILE="output/merged-classification-output.ttl"

export WORK_DIR=work
export DATA_DIR=input
export OUTPUT_DIR=output
export LOG_FILE=$WORK_DIR/interactions-classifier.log

# cleanup work dir
echo 'Cleaning previous files...'
rm -rf $WORK_DIR $OUTPUT_DIR
mkdir $WORK_DIR $OUTPUT_DIR

# run classifier
echo "Running classifier..."
java -Xmx4G -Xms2G -jar interactions-classifier-0.0.1-SNAPSHOT-onejar.jar classify \
--input $DATA_DIR/patient-data.ttl \
--class http://www.limics.org/remiames_ontology#Interaction \
--output $CLASSIFICATION_OUTPUT_FILE
echo "Done ! Result is in $CLASSIFICATION_OUTPUT_FILE"

# concat all input files with the classification output to query it
echo 'Merging classification results with all files...'
riot $DATA_DIR/* $CLASSIFICATION_OUTPUT_FILE > $MERGE_OUTPUT_FILE
echo "Done ! Result is in $MERGE_OUTPUT_FILE"

# execute a SPARQL query on the merge of all data, and output result
# echo 'Extracting requested output columns...'
# sparql --data=work/2-merge-output.ttl --query=$QUERY_DIR/output-query.rq --results=TSV > $1
# echo "Done ! Final result is in $OUTPUT_FILE"