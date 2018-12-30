rm -r testoutput/
rm *.log
jmeter -n -t MCorpusLoadTest.jmx -e -o testoutput -l mcorpus-jmeter.log
