import { join as pjoin } from 'path';
import fs = require('fs-extra');
import { v4 as uuidv4 } from 'uuid';

const dpath = pjoin(__dirname, '../lambda/dbbootstrap');
console.log(dpath);

const tmpdirname = `/tmp/${uuidv4()}`;
fs.mkdirSync(tmpdirname);

fs.copyFileSync(`${dpath}/dbbootstrap.py`, `${tmpdirname}/dbbootstrap.py`);
fs.copyFileSync(`${dpath}/mcorpus-schema.ddl`, `${tmpdirname}/mcorpus-schema.ddl`);
fs.copyFileSync(`${dpath}/mcorpus-roles.ddl`, `${tmpdirname}/mcorpus-roles.ddl`);
fs.copyFileSync(`${dpath}/mcorpus-mcuser.csv`, `${tmpdirname}/mcorpus-mcuser.csv`);

fs.copySync(`${dpath}/../psycopg2`, `${tmpdirname}/psycopg2`);