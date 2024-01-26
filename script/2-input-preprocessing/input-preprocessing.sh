export OUTPUT_RDF="output/patient-data.ttl"

# cleanup work dir
echo 'Cleaning previous files...'
rm -rf $OUTPUT_RDF
rm -rf work
mkdir work
rm -rf output
mkdir output

# Expects the RUIM inside data/medicament.ttl
# Expects input data in input/patient-data.csv
echo 'Step 1 : 1-patient-data-add-substance-from-ruim.rq'
java -jar sparql-anything-0.9.0.jar \
-q queries/1-patient-data-add-substance-from-ruim.rq \
--load data/medicament.ttl \
> work/1.csv

# Expects the quantities in data/CONVERSION_DOSE.csv
# Expects the result of previous query in work/1.csv
echo 'Step 2 : 2-add-substance-quantity.rq'
java -jar sparql-anything-0.9.0.jar \
-q queries/2-add-substance-quantity.rq \
> work/2.csv

# Expects the result of previous query in work/2.csv
echo 'Step 3 : 3-convert-to-other-susbtances.rq'
java -jar sparql-anything-0.9.0.jar \
-q queries/3-convert-to-other-susbtances.rq \
> work/3.csv

# cat the 2 and 3
echo 'Step 3bis : Concat 2+3...'
cat work/2.csv >> work/2_and_3.csv 
tail -n +2 "work/3.csv" >> work/2_and_3.csv 

# Expects the result of previous query in work/2_and_3.csv
echo 'Step 4 : 4-aggregate-on-substance-and-day.rq'
java -jar sparql-anything-0.9.0.jar \
-q queries/4-aggregate-on-substance-and-day.rq \
> work/4.csv

# Expects the result of previous query in work/4.csv
echo 'Step 5 : 5-convert-to-rdf.rq'
java -jar sparql-anything-0.9.0.jar \
-q queries/5-convert-to-rdf.rq \
> $OUTPUT_RDF

