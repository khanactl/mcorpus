SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

COPY public.mcuser (uid, created, modified, name, email, username, pswd, status) FROM stdin;
5fa4626a-6bcf-4975-b134-0a89190b6d80	2018-01-19 07:51:05.932926	\N	JPK	jpucop@gmail.com	jp	$2a$06$1JocFFXBmVabkaya8wd3e.NiYxH92kjNj/eQ1DvCC.ZD9yaDa9NKq	ACTIVE
d712f2d3-5494-472d-bdcc-4a1722a8c818	2018-01-19 07:51:05.932926	\N	PUBLIC	public@mcorpus.edu	public	$2a$06$M3ZSkJLqzmOWd6zSA6aN7.8FdSyw///v39T5eNwWoHB5yVeiqrRuG	ACTIVE
3bb05a05-44f4-4cb8-b396-ba69465f8c5a	2018-09-16 10:05:40.920256	\N	MEMBER	member@mcorpus.edu	member	$2a$06$eIiyv36hv8pFkzyD3dhPlODmcJNsOoHZebqkxtbiLeXukv2LGCKd6	ACTIVE
0ee2b7ba-1491-4b73-9409-363071185460	2018-09-16 10:07:11.716573	\N	MCORPUS	mcorpus@mcorpus.edu	mcorpus	$2a$06$nwnhMiGj3rCoH6FiOuCZHejtxaEQsrDulBLyZ2jcGFI7rD.M4Cmey	ACTIVE
7c657947-0e88-4b63-99b3-176c0cfa344c	2018-09-16 10:34:13.38306	\N	TEST	test@mcorpus.edu	test	$2a$06$JxbZhzqwNu5DzFkAjqLK/uVObkvPLJaombIUPK7HQnPXvSU4zuYTy	ACTIVE
\.
