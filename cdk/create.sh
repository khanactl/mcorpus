#!/bin/bash
set -e

# DEV env create
cdk deploy mcorpus-pipeline-dev --require-approval never
