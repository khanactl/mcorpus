#!/bin/bash

# verify the webapp is running
for i in `seq 1 3`;
do
  HTTP_CODE=`curl --write-out '%{http_code}' -o /dev/null -m 10 -q -s http://localhost:5150/index`
  if [ "$HTTP_CODE" == "200" ]; then
    echo "Successfully requested index page."
    exit 0;
  fi
  echo "Attempt to curl endpoint returned HTTP Code $HTTP_CODE. Backing off and retrying."
  sleep 5s
done
echo "Server did not come up after expected time. Failing."
exit 1
