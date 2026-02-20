--
-- PostgreSQL database dump
--

\restrict JwBwaZ4tZNlAFfIotGmEmRwrMcBcVzxczNA90mh2XLChW0MZAdwDi438sGmf8yy

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: commandes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commandes (
    id integer NOT NULL,
    fournisseur_id integer,
    date_commande timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    statut character varying(20),
    numero_lot character varying(50)
);


--
-- Name: commandes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.commandes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: commandes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.commandes_id_seq OWNED BY public.commandes.id;


--
-- Name: fournisseurs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fournisseurs (
    id integer NOT NULL,
    nom character varying(100) NOT NULL,
    contact character varying(100),
    adresse text
);


--
-- Name: fournisseurs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fournisseurs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fournisseurs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fournisseurs_id_seq OWNED BY public.fournisseurs.id;


--
-- Name: lignes_commande; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.lignes_commande (
    id integer NOT NULL,
    commande_id integer,
    medicament_id integer,
    quantite integer,
    prix_unitaire numeric(10,2)
);


--
-- Name: lignes_commande_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.lignes_commande_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: lignes_commande_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.lignes_commande_id_seq OWNED BY public.lignes_commande.id;


--
-- Name: lignes_vente; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.lignes_vente (
    id integer NOT NULL,
    vente_id integer,
    lot_id integer,
    quantite integer,
    prix_unitaire numeric(10,2)
);


--
-- Name: lignes_vente_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.lignes_vente_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: lignes_vente_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.lignes_vente_id_seq OWNED BY public.lignes_vente.id;


--
-- Name: lots; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.lots (
    id integer NOT NULL,
    medicament_id integer,
    numero_lot character varying(50),
    quantite_stock integer,
    date_peremption date,
    prix_achat numeric(10,2)
);


--
-- Name: lots_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.lots_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: lots_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.lots_id_seq OWNED BY public.lots.id;


--
-- Name: medicaments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.medicaments (
    id integer NOT NULL,
    nom_commercial character varying(100) NOT NULL,
    principe_actif character varying(100),
    forme_galenique character varying(50),
    dosage character varying(50),
    prix_public numeric(10,2),
    necessite_ordonnance boolean DEFAULT false,
    seuil_min_alerte integer
);


--
-- Name: medicaments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.medicaments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: medicaments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.medicaments_id_seq OWNED BY public.medicaments.id;


--
-- Name: utilisateurs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.utilisateurs (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password_hash character varying(64) NOT NULL,
    nom character varying(100),
    prenom character varying(100),
    role character varying(20) DEFAULT 'USER'::character varying,
    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: utilisateurs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.utilisateurs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: utilisateurs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.utilisateurs_id_seq OWNED BY public.utilisateurs.id;


--
-- Name: ventes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ventes (
    id integer NOT NULL,
    date_vente timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    total_vente numeric(10,2),
    sur_ordonnance boolean DEFAULT false
);


--
-- Name: ventes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ventes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ventes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ventes_id_seq OWNED BY public.ventes.id;


--
-- Name: commandes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commandes ALTER COLUMN id SET DEFAULT nextval('public.commandes_id_seq'::regclass);


--
-- Name: fournisseurs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fournisseurs ALTER COLUMN id SET DEFAULT nextval('public.fournisseurs_id_seq'::regclass);


--
-- Name: lignes_commande id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lignes_commande ALTER COLUMN id SET DEFAULT nextval('public.lignes_commande_id_seq'::regclass);


--
-- Name: lignes_vente id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lignes_vente ALTER COLUMN id SET DEFAULT nextval('public.lignes_vente_id_seq'::regclass);


--
-- Name: lots id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lots ALTER COLUMN id SET DEFAULT nextval('public.lots_id_seq'::regclass);


--
-- Name: medicaments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.medicaments ALTER COLUMN id SET DEFAULT nextval('public.medicaments_id_seq'::regclass);


--
-- Name: utilisateurs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisateurs ALTER COLUMN id SET DEFAULT nextval('public.utilisateurs_id_seq'::regclass);


--
-- Name: ventes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventes ALTER COLUMN id SET DEFAULT nextval('public.ventes_id_seq'::regclass);


--
-- Data for Name: commandes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.commandes (id, fournisseur_id, date_commande, statut, numero_lot) FROM stdin;
3	1	2026-02-17 15:44:41.156231	RECUE	\N
4	1	2026-02-17 16:06:29.464485	RECUE	\N
1	9	2026-02-16 19:54:16.170113	RECUE	LOT-2025-002
2	2	2026-02-16 20:04:52.563128	RECUE	LOT-DOL-2026-02
5	3	2026-02-18 15:27:07.203533	RECUE	LOT-LAM-2026-02
6	1	2026-02-18 22:02:50.250305	RECUE	LOT-LAM-2026-02
\.


--
-- Data for Name: fournisseurs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fournisseurs (id, nom, contact, adresse) FROM stdin;
1	Pharma Distribution	01 23 45 67 89	10 rue de la Santé, Paris
2	MediSupply	01 98 76 54 32	25 avenue Pasteur, Lyon
3	BioPharm	01 11 22 33 44	5 boulevard Voltaire, Marseille
4	Pharma Distribution	01 23 45 67 89	10 rue de la Santé, Paris
5	MediSupply	01 98 76 54 32	25 avenue Pasteur, Lyon
6	BioPharm	01 11 22 33 44	5 boulevard Voltaire, Marseille
7	Pharma Distribution	01 23 45 67 89	10 rue de la Santé, Paris
8	MediSupply	01 98 76 54 32	25 avenue Pasteur, Lyon
9	BioPharm	01 11 22 33 44	5 boulevard Voltaire, Marseille
10	Pharma Distribution	01 23 45 67 89	10 rue de la Santé, Paris
11	MediSupply	01 98 76 54 32	25 avenue Pasteur, Lyon
12	BioPharm	01 11 22 33 44	5 boulevard Voltaire, Marseille
13	Hugues	ugbarbierb@gmail.com	85 rue de brochant 75018
\.


--
-- Data for Name: lignes_commande; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.lignes_commande (id, commande_id, medicament_id, quantite, prix_unitaire) FROM stdin;
1	1	6	50	15.00
2	2	1	220	2.50
3	3	2	20	2.50
4	4	1	30	2.50
6	5	21	20	20.00
7	6	21	47	67.00
\.


--
-- Data for Name: lignes_vente; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.lignes_vente (id, vente_id, lot_id, quantite, prix_unitaire) FROM stdin;
6	2	3	20	3.20
7	3	16	20	3.20
8	4	8	120	4.20
9	5	6	50	3.50
11	5	5	150	3.50
12	6	3	80	3.20
13	7	11	45	2.80
14	8	10	6	12.50
15	9	29	50	4.20
16	9	21	35	4.20
17	10	28	30	2.50
18	10	30	130	2.50
\.


--
-- Data for Name: lots; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.lots (id, medicament_id, numero_lot, quantite_stock, date_peremption, prix_achat) FROM stdin;
4	3	LOT-AMX-2024-001	8	2026-08-11	3.50
7	5	LOT-VEN-2024-001	3	2026-10-11	5.50
9	7	LOT-GAV-2024-001	45	2026-04-11	4.00
12	10	LOT-KAR-2024-001	250	2027-06-11	1.00
13	10	LOT-KAR-2024-002	100	2026-03-11	1.00
18	4	LOT-IBU-2024-001	200	2027-10-11	2.00
23	8	LOT-AUG-2024-001	6	2026-12-11	8.00
24	9	LOT-LEV-2024-001	180	2027-12-11	1.50
1	1	LOT-DLP-2024-001	0	2027-08-11	1.20
2	1	LOT-DLP-2024-002	0	2028-02-11	1.20
16	2	LOT-EFF-2024-001	80	2027-02-11	1.80
8	6	LOT-SPA-2024-001	0	2027-05-11	2.50
27	2	LOT-2026-02-EFF-2	20	2027-02-17	2.50
6	4	LOT-IBU-2024-002	0	2026-04-11	2.00
5	4	LOT-IBU-2024-001	50	2027-10-11	2.00
3	2	LOT-EFF-2024-001	0	2027-02-11	1.80
11	9	LOT-LEV-2024-001	135	2027-12-11	1.50
10	8	LOT-AUG-2024-001	0	2026-12-11	8.00
31	21	LOT-LAM-2026-02	20	2027-03-25	20.00
29	6	LOT-2025-002-6	0	2026-02-19	15.00
21	6	LOT-SPA-2024-001	85	2027-05-11	2.50
28	1	LOT-DOL-2027-02-1	0	2027-02-17	2.50
30	1	LOT-DOL-2026-02	90	2027-02-18	2.50
32	21	LOT-LAM-2026-02	47	2067-02-27	67.00
\.


--
-- Data for Name: medicaments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.medicaments (id, nom_commercial, principe_actif, forme_galenique, dosage, prix_public, necessite_ordonnance, seuil_min_alerte) FROM stdin;
1	Doliprane	Paracétamol	Comprimé	1000mg	2.50	f	20
2	Efferalgan	Paracétamol	Comprimé effervescent	500mg	3.20	f	15
3	Amoxicilline	Amoxicilline	Gélule	500mg	5.80	t	10
5	Ventoline	Salbutamol	Spray	100µg	8.90	t	5
6	Spasfon	Phloroglucinol	Comprimé	80mg	4.20	f	15
7	Gaviscon	Alginate de sodium	Suspension buvable	10ml	6.50	f	10
9	Levothyrox	Lévothyroxine	Comprimé	50µg	2.80	t	20
10	Kardégic	Aspirine	Comprimé	75mg	1.90	t	30
21	Lamino	Laminoïde	Suppositoire	230mg	6.70	f	245
4	Advil	Ibuprofène	Comprimé	400mg	3.50	f	25
14	Ibuprofène MAX	Ibuprofène	Comprimé	400mg	3.50	f	25
8	Augmentin	Amoxicilline + Acide clavulanic	Comprimé	1g	12.50	t	8
\.


--
-- Data for Name: utilisateurs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.utilisateurs (id, username, password_hash, nom, prenom, role, date_creation) FROM stdin;
2	ug78	$2a$10$fk53wQOQrJ69XL4tF8zwFeqqa5.qNjXdcQA1GZjNIXmeH7oczKPAG	Barbier-Bentera	Hugues	ADMIN	2026-02-11 19:10:22.818594
3	lamine75	$2a$10$EOa/FhPfmJibaMhT1gBKKOku1138HNQXWI50tiD0Hja56iWmuR7SW	Betraoui	Lamine	USER	2026-02-12 15:57:35.041866
1	admin	$2a$10$DDFMSqniwTvzezb/.fOsrOC7l12YTMWva75bQcEV3pxoCjCn4dFAO	Administrateur	Système	ADMIN	2026-02-11 18:56:53.379178
4	Hadrio	$2a$10$twawBZN35ZlI9edKsnbaGeJEt9B5/stui58YPAjLi3mrhngHRYPmW	Barbetmoustache	Hadri	USER	2026-02-18 22:05:43.051586
\.


--
-- Data for Name: ventes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.ventes (id, date_vente, total_vente, sur_ordonnance) FROM stdin;
1	2026-02-12 16:06:47.652116	25.00	f
2	2026-02-12 16:08:15.726397	64.00	f
3	2026-02-16 13:45:35.115184	64.00	f
4	2026-02-16 14:19:41.217448	504.00	f
5	2026-02-17 15:51:36.699437	875.00	f
6	2026-02-17 16:14:47.610423	256.00	f
7	2026-02-17 20:42:52.176423	126.00	f
8	2026-02-18 01:08:02.984421	75.00	f
9	2026-02-18 15:32:19.74092	357.00	f
10	2026-02-18 15:33:04.302108	400.00	f
\.


--
-- Name: commandes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.commandes_id_seq', 6, true);


--
-- Name: fournisseurs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.fournisseurs_id_seq', 13, true);


--
-- Name: lignes_commande_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.lignes_commande_id_seq', 7, true);


--
-- Name: lignes_vente_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.lignes_vente_id_seq', 18, true);


--
-- Name: lots_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.lots_id_seq', 32, true);


--
-- Name: medicaments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.medicaments_id_seq', 23, true);


--
-- Name: utilisateurs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.utilisateurs_id_seq', 4, true);


--
-- Name: ventes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ventes_id_seq', 10, true);


--
-- Name: commandes commandes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commandes
    ADD CONSTRAINT commandes_pkey PRIMARY KEY (id);


--
-- Name: fournisseurs fournisseurs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fournisseurs
    ADD CONSTRAINT fournisseurs_pkey PRIMARY KEY (id);


--
-- Name: lignes_commande lignes_commande_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lignes_commande
    ADD CONSTRAINT lignes_commande_pkey PRIMARY KEY (id);


--
-- Name: lignes_vente lignes_vente_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lignes_vente
    ADD CONSTRAINT lignes_vente_pkey PRIMARY KEY (id);


--
-- Name: lots lots_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lots
    ADD CONSTRAINT lots_pkey PRIMARY KEY (id);


--
-- Name: medicaments medicaments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.medicaments
    ADD CONSTRAINT medicaments_pkey PRIMARY KEY (id);


--
-- Name: utilisateurs utilisateurs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisateurs
    ADD CONSTRAINT utilisateurs_pkey PRIMARY KEY (id);


--
-- Name: utilisateurs utilisateurs_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisateurs
    ADD CONSTRAINT utilisateurs_username_key UNIQUE (username);


--
-- Name: ventes ventes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventes
    ADD CONSTRAINT ventes_pkey PRIMARY KEY (id);


--
-- Name: idx_commandes_fournisseur_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_commandes_fournisseur_id ON public.commandes USING btree (fournisseur_id);


--
-- Name: idx_lignes_commande_commande_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lignes_commande_commande_id ON public.lignes_commande USING btree (commande_id);


--
-- Name: idx_lignes_commande_medicament_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lignes_commande_medicament_id ON public.lignes_commande USING btree (medicament_id);


--
-- Name: idx_lignes_vente_lot_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lignes_vente_lot_id ON public.lignes_vente USING btree (lot_id);


--
-- Name: idx_lignes_vente_vente_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lignes_vente_vente_id ON public.lignes_vente USING btree (vente_id);


--
-- Name: idx_lots_medicament_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lots_medicament_id ON public.lots USING btree (medicament_id);


--
-- Name: commandes commandes_fournisseur_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commandes
    ADD CONSTRAINT commandes_fournisseur_id_fkey FOREIGN KEY (fournisseur_id) REFERENCES public.fournisseurs(id) ON DELETE SET NULL;


--
-- Name: lignes_commande lignes_commande_commande_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lignes_commande
    ADD CONSTRAINT lignes_commande_commande_id_fkey FOREIGN KEY (commande_id) REFERENCES public.commandes(id) ON DELETE CASCADE;


--
-- Name: lignes_commande lignes_commande_medicament_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lignes_commande
    ADD CONSTRAINT lignes_commande_medicament_id_fkey FOREIGN KEY (medicament_id) REFERENCES public.medicaments(id) ON DELETE CASCADE;


--
-- Name: lignes_vente lignes_vente_lot_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lignes_vente
    ADD CONSTRAINT lignes_vente_lot_id_fkey FOREIGN KEY (lot_id) REFERENCES public.lots(id) ON DELETE CASCADE;


--
-- Name: lignes_vente lignes_vente_vente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lignes_vente
    ADD CONSTRAINT lignes_vente_vente_id_fkey FOREIGN KEY (vente_id) REFERENCES public.ventes(id) ON DELETE CASCADE;


--
-- Name: lots lots_medicament_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lots
    ADD CONSTRAINT lots_medicament_id_fkey FOREIGN KEY (medicament_id) REFERENCES public.medicaments(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict JwBwaZ4tZNlAFfIotGmEmRwrMcBcVzxczNA90mh2XLChW0MZAdwDi438sGmf8yy

