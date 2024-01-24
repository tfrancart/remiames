# 1. Run RUIM preprocessing
# cd 1-ruim-preprocessing
# ./ruim-preprocessing.sh
# cd ..
# cp 1-ruim-preprocessing/output/medicament.ttl 2-input-preprocessing/data

echo "**** Input pre-processing... ****"
cd 2-input-preprocessing
./input-preprocessing.sh
cd ..
cp 2-input-preprocessing/output/patient-data.ttl 3-interactions-classifier/input

echo "**** Classification to detect interactions... ****"
cd 3-interactions-classifier
./interactions-classifier.sh
cd ..
cp 3-interactions-classifier/output/merged-classification-output.ttl 4-output-postprocessing/input

echo "**** Output formatting... ****"
cd 4-output-postprocessing
./output-postprocessing.sh
cd ..
echo "Done ! final result is under 4-output-postprocessing/output"