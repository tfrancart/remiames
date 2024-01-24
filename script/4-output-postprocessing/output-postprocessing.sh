export OUTPUT_FILE="output/interactions.tsv"

export INPUT_DIR=input
export QUERY_DIR=query
export OUTPUT_DIR=output

# cleanup work dir
echo 'Cleaning previous files...'
rm -rf $OUTPUT_DIR 
mkdir $OUTPUT_DIR

# execute a SPARQL query on the merge of all data, and output result
echo 'Extracting requested output columns...'
sparql \
--data=input/merged-classification-output.ttl \
--query=$QUERY_DIR/output-query.rq \
--results=TSV \
> $OUTPUT_FILE
echo "Done ! Final result is in $OUTPUT_FILE"