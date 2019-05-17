SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

COPY public.mcuser (uid, created, modified, name, email, username, pswd, status, roles) FROM stdin;
5fa4626a-6bcf-4975-b134-0a89190b6d80	2018-01-19 07:51:05.932926	\N	JPK	jpucop@gmail.com	jp	$2a$06$1JocFFXBmVabkaya8wd3e.NiYxH92kjNj/eQ1DvCC.ZD9yaDa9NKq	ACTIVE	{ ADMIN }
3bb05a05-44f4-4cb8-b396-ba69465f8c5a	2018-09-16 10:05:40.920256	\N	MEMBER	member@mcorpus.edu	member	$2a$06$eIiyv36hv8pFkzyD3dhPlODmcJNsOoHZebqkxtbiLeXukv2LGCKd6	ACTIVE	{ MEMBER }
0ee2b7ba-1491-4b73-9409-363071185460	2018-09-16 10:07:11.716573	\N	MCORPUS	mcorpus@mcorpus.edu	mcorpus	$2a$06$nwnhMiGj3rCoH6FiOuCZHejtxaEQsrDulBLyZ2jcGFI7rD.M4Cmey	ACTIVE	{ MCORPUS }
ddea0485-0448-4e8c-b2d6-b04c209d8a48	2018-12-14 20:09:58.12158	\N	AWS Demo user	admin@mcorpus.edu	demo	$2a$06$y1BiZvE3h/v5WxrUgTNik.gYQYea2uUcd4tGKKiuxd3KifZFSxCLS	ACTIVE	{ MCORPUS, MPII }
46865820-0c86-4d95-986c-13de02d9f778	2018-12-16 12:38:15.000058	\N	Load test	admin@mcorpus.edu	loadtest	$2a$06$XcOXny1RDifiFGQbV0NnkOCZaCaOJSwO/ieozj8LWt8Ot1vOesHDO	ACTIVE	{ MCORPUS, MPII }
\.
