
export OUTPUT_FILE=output/medicament.ttl

echo 'Cleaning previous files...'
rm -rf work
mkdir work
rm -rf output
mkdir output

echo 'Getting clean version of RUIM...'
riot input/* > work/ruim-full.ttl
echo 'Extracting subset of RUIM...'
sparql --data=work/ruim-full.ttl --query=queries/extract-ruim.rq --results=turtle > work/ruim-small.ttl
echo 'Converting substances actives into OWL axioms...'
sparql --data=work/ruim-small.ttl --query=queries/update-ruim-sa.rq --results=turtle > work/ruim-sa.ttl
echo 'Converting fractions therapeutiques into OWL axioms...'
sparql --data=work/ruim-small.ttl --query=queries/update-ruim-ft.rq --results=turtle > work/ruim-ft.ttl
echo 'Merging into final file...'
riot --formatted=turtle work/ruim-small.ttl work/ruim-sa.ttl work/ruim-ft.ttl > $OUTPUT_FILE
echo "Done preprocessing ! final output file is in '$OUTPUT_FILE'"