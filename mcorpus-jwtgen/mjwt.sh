#!/bin/bash

# generate a JWT for mcorpus access

# call the mcorpus-jwtgen shaded jar
java -jar target/mcorpus-jwtgen.jar "$@"