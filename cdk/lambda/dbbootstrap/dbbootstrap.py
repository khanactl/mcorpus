def main(event, context):
  import json
  import logging as log
  import secrets

  import boto3
  import psycopg2
  from botocore.exceptions import ClientError

  log.getLogger().setLevel(log.INFO)

  response = {
    'Data': {
      'SsmNameJdbcUrl': '',
      'SsmNameJdbcTestUrl': '',
      'SsmVersionJdbcUrl': '',
      'SsmVersionJdbcTestUrl': '',
      'Message': ''
    },
    'PhysicalResourceId': 'McorpusDbBootstrap' + event['ResourceProperties']['AppEnv']
  }

  try:
    log.info('Input event: %s', event)

    # CREATE
    if event['RequestType'] == 'Create':
      # Check if this is a Create and we're failing Creates
      if event['ResourceProperties'].get('FailCreate', False):
        raise RuntimeError('Create failure requested')

      log.info('CREATE request')
      # input: aws region for both getting db rds secret and creating ssm jdbc secure params
      region_name = event['ResourceProperties']['TargetRegion']

      # input: db rds json secret
      db_secret_arn = event['ResourceProperties']['DbJsonSecretArn']

      ssmNameJdbcAdminUrl = event['ResourceProperties']['SsmNameJdbcAdminUrl']
      ssmNameJdbcUrl = event['ResourceProperties']['SsmNameJdbcUrl']
      ssmNameJdbcTestUrl = event['ResourceProperties']['SsmNameJdbcTestUrl']

      session = boto3.session.Session()

      secretsClient = session.client(
        service_name = 'secretsmanager',
        region_name = region_name
      )

      # pull the db rds secret json object
      try:
        get_secret_value_response = secretsClient.get_secret_value(
          SecretId = db_secret_arn
        )
      except ClientError as e:
        log.error(e)
        response['Data']['Message'] = str(e)
        return response
      else:
        # Decrypts secret using the associated KMS CMK.
        # Depending on whether the secret is a string or binary, one of these fields will be populated.
        if 'SecretString' in get_secret_value_response:

          secret = json.loads(get_secret_value_response['SecretString'])
          db_host = secret['host']
          db_port = secret['port']
          db_name = secret['dbname']
          db_masteruser = secret['username']
          db_masterpswd = secret['password']
          log.info("db: {dbHost}:{dbPort}/{dbName} ({dbMasterUser})".format(
            dbHost = db_host,
            dbPort = db_port,
            dbName = db_name,
            dbMasterUser = db_masteruser
          ))

          cursor = 0
          conn = 0

          try:
            # db connect
            conn = psycopg2.connect(
              host = db_host,
              database = db_name,
              user = db_masteruser,
              password = db_masterpswd
            )
            cursor = conn.cursor()
            log.info('db connected')

            # generate rando mcweb and mcwebtest db user passwords
            mcadmin = secrets.token_urlsafe(16)
            mcweb = secrets.token_urlsafe(16)
            mcwebtest = secrets.token_urlsafe(16)

            # run the schema DDL file
            cursor.execute(open("mcorpus-schema.ddl", "r").read())
            log.info('db schema DDL file executed')

            # run the db user roles file
            cursor.execute(open("mcorpus-roles.ddl", "r").read()
              .replace("{mcadmin}", mcadmin)
              .replace("{mcweb}", mcweb)
              .replace("{mcwebtest}", mcwebtest)
            )
            log.info('db user roles SQL file executed')

            # insert the default mcuser records
            with open("mcorpus-mcuser.csv", 'r') as f:
              cursor.copy_expert("COPY mcuser from STDIN CSV HEADER NULL '\\N';", f)
            log.info('default mcuser records inserted')

            # commit
            conn.commit()
            log.info('db mutations committed')

          except Exception as e:
            log.error(e)
            response['Data']['Message'] = str(e)
            return response
          finally:
            if(cursor):
              cursor.close()
            if(conn):
              conn.close()

          # assemble jdbc urls
          jdbcAdminUrl = "jdbc:postgresql://{dbHost}:{dbPort}/{dbName}?user={dbUser}&password={dbPswd}&ssl=false".format(
            dbHost = db_host,
            dbPort = db_port,
            dbName = db_name,
            dbUser = 'mcadmin',
            dbPswd = mcadmin
          )
          jdbcUrl = "jdbc:postgresql://{dbHost}:{dbPort}/{dbName}?user={dbUser}&password={dbPswd}&ssl=false".format(
            dbHost = db_host,
            dbPort = db_port,
            dbName = db_name,
            dbUser = 'mcweb',
            dbPswd = mcweb
          )
          jdbcTestUrl = "jdbc:postgresql://{dbHost}:{dbPort}/{dbName}?user={dbUser}&password={dbPswd}&ssl=false".format(
            dbHost = db_host,
            dbPort = db_port,
            dbName = db_name,
            dbUser = 'mcwebtest',
            dbPswd = mcwebtest
          )

          ssmClient = session.client(
            service_name = 'ssm',
            region_name = region_name
          )

          # generate ssm jdbc admin url
          ssmResponseJdbcAdminUrl = ssmClient.put_parameter(
            Name = ssmNameJdbcAdminUrl,
            Value = jdbcAdminUrl,
            Type = 'SecureString',
            Overwrite = True,
          )
          ssmJdbcAdminUrlVersion = ssmResponseJdbcAdminUrl['Version']
          log.info('ssmJdbcAdminUrlVersion: {ssmJdbcAdminUrlVersion}'.format(ssmJdbcAdminUrlVersion = ssmJdbcAdminUrlVersion))

          # generate ssm jdbc url
          ssmResponseJdbcUrl = ssmClient.put_parameter(
            Name = ssmNameJdbcUrl,
            Value = jdbcUrl,
            Type = 'SecureString',
            Overwrite = True,
          )
          ssmJdbcUrlVersion = ssmResponseJdbcUrl['Version']
          log.info('ssmJdbcUrlVersion: {ssmJdbcUrlVersion}'.format(ssmJdbcUrlVersion = ssmJdbcUrlVersion))

          # generate ssm jdbc TEST url
          ssmResponseJdbcTestUrl = ssmClient.put_parameter(
            Name = ssmNameJdbcTestUrl,
            Value = jdbcTestUrl,
            Type = 'SecureString',
            Overwrite = True,
          )
          ssmJdbcTestUrlVersion = ssmResponseJdbcTestUrl['Version']
          log.info('ssmJdbcTestUrlVersion: {ssmJdbcTestUrlVersion}'.format(ssmJdbcTestUrlVersion = ssmJdbcTestUrlVersion))

        else:
          # decoded_binary_secret = base64.b64decode(get_secret_value_response['SecretBinary'])
          raise Exception('error', 'No SecretString found.')

      response['Data']['SsmNameJdbcAdminUrl'] = ssmNameJdbcAdminUrl
      response['Data']['SsmNameJdbcUrl'] = ssmNameJdbcUrl
      response['Data']['SsmNameJdbcTestUrl'] = ssmNameJdbcTestUrl

      response['Data']['SsmVersionJdbcAdminUrl'] = ssmJdbcAdminUrlVersion
      response['Data']['SsmVersionJdbcUrl'] = ssmJdbcUrlVersion
      response['Data']['SsmVersionJdbcTestUrl'] = ssmJdbcTestUrlVersion

      response['Data']['Message'] = 'Db bootstrap completed.'
      log.info('Db bootstrap completed.')

      # CREATE success
      return response

    # UPDATE
    elif event['RequestType'] == 'Update':
      log.info('UPDATE request')
      response['Data']['Message'] = 'Update is a no-op.'
      return response

    # DELETE
    elif event['RequestType'] == 'Delete':
      log.info('DELETE request')
      response['Data']['Message'] = 'Delete is a no-op.'
      return response

    # default
    else:
      msg = "Unrecognized request type: {reqType}.".format(reqType = event['RequestType'])
      log.info("FAIL - {msg}".format(msg = msg))
      response['Data']['Message'] = msg
      return response

  except Exception as e:
    log.exception(e)
    response['Data']['Message'] = str(e)
    return response
