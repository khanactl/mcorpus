SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

COPY public.mcuser_roles (uid, role) FROM stdin;
5fa4626a-6bcf-4975-b134-0a89190b6d80	MCORPUS
5fa4626a-6bcf-4975-b134-0a89190b6d80	MPII
d712f2d3-5494-472d-bdcc-4a1722a8c818	PUBLIC
3bb05a05-44f4-4cb8-b396-ba69465f8c5a	MEMBER
0ee2b7ba-1491-4b73-9409-363071185460	MCORPUS
7c657947-0e88-4b63-99b3-176c0cfa344c	MCORPUS
\.
