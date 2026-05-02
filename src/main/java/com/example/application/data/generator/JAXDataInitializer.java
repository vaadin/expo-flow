package com.example.application.data.generator;

import com.example.application.data.entity.Talk;
import com.example.application.data.repository.TalkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.example.application.data.entity.Talk.Track.*;

@Component
public class JAXDataInitializer implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(JAXDataInitializer.class);

    private final TalkRepository talkRepository;

    public JAXDataInitializer(TalkRepository talkRepository) {
        this.talkRepository = talkRepository;
    }

    @Override
    public void run(String... args) {
        if (talkRepository.count() > 0) {
            logger.info("Talks already initialized");
            return;
        }

        logger.info("Initializing talks");

        LocalDate d04 = LocalDate.of(2026, 5, 4);
        LocalDate d05 = LocalDate.of(2026, 5, 5);
        LocalDate d06 = LocalDate.of(2026, 5, 6);
        LocalDate d07 = LocalDate.of(2026, 5, 7);
        LocalDate d08 = LocalDate.of(2026, 5, 8);

        // ── 04 May 2026 ───────────────────────────────────────────────────────

        t("Lean Coffee Session: Deine Erfahrung zählt!",
                "Uwe Friedrichsen, Wolfgang Pleus, Pascal Euhus, Sarah Schuh, Sven Müller",
                Talk.Format.SESSION, d04, h(15,30), h(17,0), "Watford",
                AGILE);

        t("GenAI in der Praxis: Ein Workshop für robuste, produktionsreife AI-Services",
                "Lars Röwekamp, Tim Wüllner",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Gutenberg 1",
                DATA_ML, GEN_AI);

        t("Angular 2026 Workshop: Signal Store, Signal Forms und AI-Assistenten",
                "Manfred Steyer, Yara Mayer",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Meetingraum II A",
                WEB_JS);

        t("Digitale Souveränität: Warum sie bei Entwicklern beginnt",
                "Sven Müller",
                Talk.Format.SESSION, d04, h(9,45), h(10,30), "Watford",
                AGILE, ARCH);

        t("Lehren aus 10 Jahre Microservices: Workshop für fortgeschrittene Architektur auch für Monolithen",
                "Eberhard Wolff",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Gutenberg 3",
                ARCH, MICRO);

        t("Workshop: smarte Anwendungen entwickeln mit Spring AI",
                "Thorben Janssen",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Gutenberg 4",
                SERVER_JAVA);

        t("Workshop: Taktisches DDD mit Spring und jMolecules leichtgewichtig implementieren",
                "Stefan Heinzer",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Zagreb A",
                SERVER_JAVA, ARCH, MICRO);

        t("Workshop: KI in Softwarearchitekturen",
                "Bernd Fondermann",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Gutenberg 2",
                ARCH, GEN_AI);

        t("Klingt gut. Passt schon. – Wie leises Schweigen laute Konflikte erzeugt",
                "Sarah Schuh",
                Talk.Format.SESSION, d04, h(14,15), h(15,0), "Watford",
                AGILE, ARCH);

        t("Web-Security-Fundamentals – Hands-on-Workshop",
                "Martina Kraus",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Zagreb B",
                PERF_SEC, WEB_JS);

        t("KI in der Software Entwicklung erfolgreich einführen: Vom Experiment zur Transformation",
                "Pascal Euhus",
                Talk.Format.SESSION, d04, h(11,0), h(11,45), "Watford",
                AGILE, ARCH, GEN_AI);

        t("Workshop: Python-Quick-Start für den vielbeschäftigten Java-Entwickler",
                "Michael Inden",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Meetingraum II B",
                CORE_JAVA);

        t("Begrüßung zum Agile Flow Day",
                "Wolfgang Pleus",
                Talk.Format.SESSION, d04, h(9,30), h(9,45), "Watford",
                AGILE);

        t("Product-Discovery-Techniken, die Entwicklungs-Teams kennen sollten",
                "Konstantin Diener",
                Talk.Format.SESSION, d04, h(11,45), h(12,30), "Watford",
                AGILE_FLOW, ARCH);

        t("Die 7 Fallstricke der KI",
                "Uwe Friedrichsen",
                Talk.Format.SESSION, d04, h(13,30), h(14,15), "Watford",
                GEN_AI);

        t("Workshop: Becoming the Godfather of Threat Modeling [ONSITE ONLY]",
                "Mike van der Bijl",
                Talk.Format.WORKSHOP, d04, h(9,0), h(17,0), "Dijon",
                PERF_SEC);

        // ── 05 May 2026 ───────────────────────────────────────────────────────

        t("Begrüßung und Eröffnungskeynote zur JAX 2026 in Mainz",
                "Sebastian Meyen",
                Talk.Format.KEYNOTE, d05, h(9,0), h(9,30), "Kongresssaal",
                SERVER_JAVA, PERF_SEC, AGILE, CLOUD, WEB_JS, DEVOPS, DATA_ML, CORE_JAVA, ARCH, MICRO, GEN_AI);

        t("AI Literacy for Developers: Thinking Clearly While AI Writes the Code",
                "Russell Miles",
                Talk.Format.KEYNOTE, d05, h(9,30), h(10,0), "Kongresssaal",
                SERVER_JAVA, PERF_SEC, AGILE, CLOUD, WEB_JS, DEVOPS, DATA_ML, CORE_JAVA, ARCH, MICRO);

        t("Ethical Code: Building Software with Integrity and Responsibility",
                "Henning Schwentner",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Center Stage",
                AGILE, ARCH);

        t("Architectural Intelligence: Ist AI die bessere Softwarearchitektin?",
                "Lars Röwekamp",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Kongresssaal",
                DATA_ML, ARCH, GEN_AI);

        t("Spring AI in der Praxis: RAG, MCP und LLM-Evaluation",
                "Thorben Janssen",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Gutenberg 1",
                SERVER_JAVA, GEN_AI);

        t("Distributed Tracing – moderne Observability für Java-Teams",
                "Thilo Frotscher",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Gutenberg 2",
                SERVER_JAVA, MICRO);

        t("Angular-Architekturen neu gedacht: Die Zukunft mit Signals, Signal Store und der neuen Resource API",
                "Manfred Steyer",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Gutenberg 4",
                WEB_JS);

        t("Passkeys für Entwickler:innen – die Zukunft ohne Passwörter",
                "Martina Kraus",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Gutenberg 3",
                PERF_SEC, WEB_JS);

        t("Trusted by Default: Die Illusion sicherer Java-Libraries und das Ende der Blackbox",
                "Moritz Meid",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Zagreb A",
                PERF_SEC, DEVOPS);

        t("Possessed by Packages: Is Your JavaScript Haunted?",
                "Chris DeMars",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Zagreb B",
                PERF_SEC, WEB_JS);

        t("Best of Modern Java 25 LTS und 26 – meine Lieblingsfeatures",
                "Michael Inden",
                Talk.Format.SESSION, d05, h(10,30), h(11,30), "Watford",
                CORE_JAVA);

        t("Live podcast: What Happens When You Add AI to a Crisis?",
                "Sebastian Meyen, Uwe Friedrichsen, Michael Dowden",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Center Stage",
                AGILE, DEVOPS, ARCH, GEN_AI);

        t("Modularisieren und Modernisieren von Legacy-Software",
                "Henning Schwentner",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Gutenberg 1",
                AGILE_FLOW, ARCH, MICRO);

        t("Concurrency aus Architektursicht",
                "Arno Haase",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Gutenberg 2",
                SERVER_JAVA, CORE_JAVA, ARCH);

        t("Über den Tellerrand von SQL - Moderne Postgres Features Live erkundet",
                "Christian Wörz",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Gutenberg 3",
                PERF_SEC, DATA_ML);

        t("Bootiful Spring AI",
                "Josh Long",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Gutenberg 4",
                SERVER_JAVA, CLOUD, CORE_JAVA, GEN_AI);

        t("AI4DevOps: Wie KI den Software-Lifecycle wirklich verändert",
                "Alexander Hofmann",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Zagreb B",
                AGILE, DEVOPS, GEN_AI);

        t("Von Sicherheitslücken und Compliance – Erfolgsfaktoren für SAST in der Praxis",
                "Dr. Tobias Röhm",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Zagreb A",
                PERF_SEC, DEVOPS, ARCH);

        t("Business Logic First: Escaping Java's Bloat Addiction",
                "Adam Bien",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Watford",
                SERVER_JAVA, AGILE, ARCH, MICRO);

        t("KI: Ein Werkzeug für Architektur?",
                "Eberhard Wolff",
                Talk.Format.SESSION, d05, h(12,0), h(13,0), "Kongresssaal",
                ARCH, MICRO, GEN_AI);

        t("Bugfix für Verhandlungen: warum wir über Features reden, aber über Interessen streiten [on-site only]",
                "Tal Uscher",
                Talk.Format.LAB, d05, h(12,0), h(13,0), "Dijon",
                AGILE);

        t("Panel - Microservices & Modularisierung: Lehren aus 10 Jahren",
                "Lutz Hühnken, Arno Haase, Eberhard Wolff, Henning Schwentner, Falk Sippach",
                Talk.Format.PANEL, d05, h(13,15), h(14,0), "Center Stage",
                ARCH, MICRO);

        t("From Mainframe to Modular: The Developer's Guide to Legacy Modernization",
                "Lars Reichert, Hergy Fongue",
                Talk.Format.KEYNOTE, d05, h(14,15), h(14,45), "Kongresssaal",
                AGILE, DEVOPS, ARCH);

        t("Live Coding Production Java: Incremental Development with LLMs",
                "Adam Bien",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Kongresssaal",
                SERVER_JAVA, ARCH, GEN_AI);

        t("JDK LTS Release Features: Warum sollten Java Anwendungen auf dem neuesten Stand sein?",
                "Wolfgang Weigend",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Gutenberg 1",
                SERVER_JAVA);

        t("Low-Code trifft Agentic AI: Prozess-Orchestrierung, UI-Generierung und Conversational AI auf Enterprise-Level",
                "Marvin Stoppelkötter, Nils Saphörster",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Gutenberg 2",
                AGILE, ARCH, MICRO, GEN_AI);

        t("TypeScript Deep Dive: Selten genutzte, fortgeschrittene Features verständlich erklärt",
                "Christian Wörz",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Gutenberg 3",
                WEB_JS);

        t("Bootiful Spring Boot: A DOGumentary",
                "Josh Long",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Gutenberg 4",
                SERVER_JAVA, CLOUD, CORE_JAVA, GEN_AI);

        t("Machine Learning & Micro-Metrics to Forecast Production Problems",
                "Ram Lakshmanan",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Zagreb A",
                SERVER_JAVA, DATA_ML, CORE_JAVA);

        t("Serverless Java Web Applications on AWS Lambda With Micronaut, Quarkus and Spring Boot",
                "Vadym Kazulkin",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Zagreb B",
                PERF_SEC, CLOUD, ARCH);

        t("KI-Tech in Software – Deep Dive",
                "Bernd Fondermann",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Watford",
                DATA_ML, ARCH, GEN_AI);

        t("Application Security für die Vertriebsplattform der Deutschen Bahn",
                "Christian Georg",
                Talk.Format.SESSION, d05, h(15,15), h(16,15), "Center Stage",
                PERF_SEC, DEVOPS, ARCH);

        t("Local-First: ein altes Paradigma, weitergedacht",
                "Yara Mayer",
                Talk.Format.SESSION, d05, h(16,45), h(17,45), "Center Stage",
                WEB_JS, ARCH, MICRO);

        t("Wie sich Java durch Value Objects weiterentwickeln wird",
                "Falk Sippach",
                Talk.Format.SESSION, d05, h(16,45), h(17,45), "Watford",
                CORE_JAVA);

        t("Schütze dein Frontend: Warum Tokens im Browser eine schlechte Idee sind",
                "Martina Kraus",
                Talk.Format.SESSION, d05, h(16,45), h(17,45), "Gutenberg 4",
                PERF_SEC, WEB_JS);

        t("Tests für moderne KI-Features: praktische Vorgehensweisen mit Spring AI",
                "Thorben Janssen",
                Talk.Format.SESSION, d05, h(16,45), h(17,45), "Gutenberg 2",
                SERVER_JAVA, GEN_AI);

        t("Entwicklung des Finanziererportals: Lessons Learned aus einem Startup-Projekt",
                "Adrian Möller, Hubert Corr",
                Talk.Format.SESSION, d05, h(16,45), h(17,45), "Zagreb B",
                CLOUD, ARCH, MICRO);

        t("Wenn Bots den Code schreiben – Rollenwandel im Team mit KI-Unterstützung",
                "Lars Vogel, Jennifer Nerlich",
                Talk.Format.SESSION, d05, h(16,45), h(17,45), "Gutenberg 3",
                AGILE, GEN_AI);

        t("Vergesst technische Schulden",
                "Uwe Friedrichsen",
                Talk.Format.SESSION, d05, h(16,45), h(17,45), "Kongresssaal",
                AGILE, ARCH);

        t("Software mit System - Systems Thinking, Co-Creation und Liberating Structures in der Anwendung [Onsite Only Lab]",
                "Wolfgang Pleus",
                Talk.Format.LAB, d05, h(16,45), h(19,15), "Dijon",
                AGILE, ARCH, GEN_AI);

        t("Agentic AI im Frontend: Architekturen mit offenen Standards",
                "Manfred Steyer",
                Talk.Format.SESSION, d05, h(18,15), h(19,15), "Gutenberg 4",
                WEB_JS, GEN_AI);

        t("Effizienz, Effizienz, Effizienz! - Web-Frontends für Geschäftsanwendungen",
                "Björn Müller",
                Talk.Format.SESSION, d05, h(18,15), h(19,15), "Gutenberg 3",
                WEB_JS, ARCH);

        t("Orientierung im Event-Land – Klarheit im Buzzword-Dschungel",
                "Frank Steimle, Florian Pfleiderer",
                Talk.Format.SESSION, d05, h(18,15), h(19,15), "Zagreb A",
                SERVER_JAVA);

        t("Verschlüsselung in der Praxis: Sichere Kommunikation mit Spring Boot, X.509 und SSL",
                "Christian Grümme",
                Talk.Format.SESSION, d05, h(18,15), h(19,15), "Watford",
                PERF_SEC);

        t("AI-boosted Legacy-Modernisierung",
                "Bernd Rederlechner",
                Talk.Format.SESSION, d05, h(18,15), h(19,15), "Gutenberg 1",
                ARCH, GEN_AI);

        t("Individualsoftware vom Laufband mit KI-Agenten: ein Erfahrungsbericht",
                "Stefan Heinzer",
                Talk.Format.SESSION, d05, h(18,15), h(19,15), "Kongresssaal",
                GEN_AI);

        t("Java Native Memory Leaks & How to Fix Them",
                "Ram Lakshmanan",
                Talk.Format.SESSION, d05, h(18,15), h(19,15), "Gutenberg 2",
                SERVER_JAVA, CORE_JAVA);

        t("Panel: Digitale Souveränität: Open Source, Cloud-Kosten, Geopolitik — wem gehört unsere Infrastruktur?",
                "Uwe Friedrichsen, Torsten Bøgh Köster, Sandra Parsick, Peter Roßbach",
                Talk.Format.PANEL, d05, h(19,5), h(19,50), "Center Stage",
                CLOUD);

        t("Omega Programming: KI-Systeme führen, die Code schreiben",
                "Paul Dubs",
                Talk.Format.KEYNOTE, d05, h(20,15), h(21,0), "Kongresssaal",
                ARCH, GEN_AI);

        // ── 06 May 2026 ───────────────────────────────────────────────────────

        t("Descartes' Daughter: How We Taught Machines to Feel (and Why We Believed Them)",
                "Jeff Watkins",
                Talk.Format.KEYNOTE, d06, h(9,0), h(9,45), "Kongresssaal",
                PERF_SEC, AGILE, DATA_ML, ARCH, GEN_AI);

        t("The Men in Black Know What's in Your JavaScript and How to Fix it!",
                "Chris DeMars",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Center Stage",
                PERF_SEC, WEB_JS, DEVOPS);

        t("Architektur für KI‑gestützte Entwicklung — Erkenntnisse aus dem DORA-Report",
                "Sebastian Hirschmeier",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Watford",
                AGILE, ARCH, GEN_AI);

        t("Kubernetes – das Rückgrat moderner Cloud-Native-Plattformen",
                "Peter Roßbach",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Gutenberg 3",
                CLOUD);

        t("Quarkus kennenlernen: Es muss nicht immer Spring Boot sein",
                "Thilo Frotscher",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Gutenberg 4",
                SERVER_JAVA);

        t("AI für Frontend-Entwickler",
                "Karsten Sitterberg",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Gutenberg 1",
                WEB_JS, GEN_AI);

        t("Architektur Reviews – Architektur verstehen heißt Teams verstehen",
                "Eberhard Wolff",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Kongresssaal",
                ARCH, MICRO);

        t("Wie kann ich Java schneller starten - und wann lohnt sich das?",
                "Karsten Silz",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Zagreb A",
                PERF_SEC);

        t("Zeit wird's! Praktische Tipps für den Umgang mit temporaler Komplexität",
                "Christian Seifert",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Zagreb B",
                SERVER_JAVA, CORE_JAVA, ARCH);

        t("Modernes Java: Wie Sprache und Laufzeit Zuverlässigkeit im Software Engineering fördern",
                "Michael Inden",
                Talk.Format.SESSION, d06, h(10,15), h(11,15), "Gutenberg 2",
                SERVER_JAVA, CORE_JAVA);

        t("Jira -> GitHub Issues: Convincing Your Product Owners to Make the Switch",
                "Samantha Casler",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Center Stage",
                AGILE, DEVOPS);

        t("Threat Modeling agentenbasierter KI-Systeme in fünf Bedrohungszonen",
                "Christian Schneider",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Gutenberg 1",
                PERF_SEC, DEVOPS, GEN_AI);

        t("Wenn Architekturgrenzen verschwimmen: Warum Enterprise und Software Architecture einander brauchen",
                "Mike Wiesner",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Gutenberg 2",
                ARCH);

        t("Microservices Need Microworkflows",
                "Lutz Hühnken",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Gutenberg 3",
                SERVER_JAVA, ARCH, MICRO);

        t("Effective Java, revisited for Java 25 and 26",
                "Frederieke Scheper",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Kongresssaal",
                CORE_JAVA);

        t("Lifting Large Language Models on Kubernetes",
                "Roland Huß",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Zagreb B",
                DEVOPS, GEN_AI);

        t("Datenbank-Auditierung mit Spring Data JPA und Envers",
                "Julius Mischok",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Zagreb A",
                SERVER_JAVA, CORE_JAVA);

        t("Cloud-Native Entwicklungsprozesse: Workflows, Architektur und Zusammenarbeit neu gedacht",
                "Robin Ferrari, Stefan Wilk",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Watford",
                AGILE, CLOUD, DEVOPS);

        t("Qualität trotz immer kürzerer Releasezyklen - mit Continuous Quality Control",
                "Dr. Tobias Röhm",
                Talk.Format.SESSION, d06, h(11,45), h(12,45), "Gutenberg 4",
                AGILE, DEVOPS);

        t("Panel - Vibe Coding & Spec-Driven Dev: Revolution im Entwickleralltag — oder heiße Luft?",
                "Julius Mischok, Simon Martinelli, Lars Röwekamp, Karsten Silz, Paul Dubs",
                Talk.Format.PANEL, d06, h(13,0), h(13,45), "Center Stage",
                SERVER_JAVA, AGILE, GEN_AI);

        t("Turbulenzen auf 30.000 Fuß: Unsere Plattform-Migration in die Public Cloud bei laufendem Betrieb",
                "Michael Schöck, Christian Kemper",
                Talk.Format.KEYNOTE, d06, h(14,0), h(14,45), "Kongresssaal",
                CLOUD, ARCH, MICRO);

        t("Terraform your Mindset: Vom Admin-Keller zur Hybrid-Cloud",
                "Maximilian Wörner, Lukas Essig",
                Talk.Format.SESSION, d06, h(15,15), h(16,15), "Zagreb A",
                AGILE, CLOUD, DEVOPS);

        t("Was ich aus 9 Produktionsanwendungen mit Claude Code gelernt habe",
                "Karsten Silz",
                Talk.Format.SESSION, d06, h(15,15), h(16,15), "Kongresssaal",
                AGILE, ARCH, GEN_AI);

        t("Agentic RAG: das Beste aus zwei (AI-)Welten",
                "Lars Röwekamp",
                Talk.Format.SESSION, d06, h(15,15), h(16,15), "Watford",
                DATA_ML, ARCH, GEN_AI);

        t("Das Frontend im Backend: Next.js für Java-Entwickler:innen",
                "Nils Hartmann",
                Talk.Format.SESSION, d06, h(15,15), h(16,15), "Gutenberg 2",
                WEB_JS);

        t("Tschüss Microservices, hallo Self-contained Systems",
                "Simon Martinelli",
                Talk.Format.SESSION, d06, h(15,15), h(16,15), "Gutenberg 1",
                ARCH, MICRO);

        t("Kubernetes – zu komplex oder genau richtig?",
                "Thomas Kruse",
                Talk.Format.SESSION, d06, h(15,15), h(16,15), "Gutenberg 4",
                PERF_SEC, AGILE, CLOUD, DEVOPS, ARCH);

        t("Ich deploye immer freitags nachmittags!",
                "Martin Kalsow, Max Edenharter",
                Talk.Format.SESSION, d06, h(15,15), h(16,15), "Center Stage",
                DEVOPS, ARCH, MICRO);

        t("Code lesen – kann doch jeder!?",
                "Ronald Brill",
                Talk.Format.SESSION, d06, h(15,15), h(16,15), "Gutenberg 3",
                AGILE, CORE_JAVA, ARCH);

        t("Kann man Energieeffizienz mit Hilfe von Architektur erzielen?",
                "Peter Kutschera, David Kopp",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Zagreb A",
                PERF_SEC, ARCH);

        t("Rethinking Software Modernization with AI",
                "Igor Gembula",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Gutenberg 3",
                WEB_JS, ARCH, GEN_AI);

        t("Angular und KI-gestützte UX: AI-assisted Interactions",
                "Mario Trzensky",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Gutenberg 1",
                WEB_JS, ARCH, GEN_AI);

        t("Entscheiden unter Unsicherheit – Psychologie, Risiko und rationale Irrtümer in der IT",
                "Milena Fluck, Andy Schmidt",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Center Stage",
                AGILE, ARCH);

        t("Integrationstests von Microservices – sogar mit Coverage-Kennzahlen",
                "Michael Hofmann, Jonas Hofmann",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Watford",
                SERVER_JAVA, CLOUD, ARCH, MICRO);

        t("Wer auf den Vibe wartet, verliert den Faden",
                "Julius Mischok",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Gutenberg 4",
                AGILE, GEN_AI);

        t("Coding challenge: AI-test vs PI-test",
                "Frederieke Scheper",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Gutenberg 2",
                SERVER_JAVA, CORE_JAVA);

        t("Building a Real-Time Collaborative Editor",
                "Dr. Mihaela Gheorghe-Roman",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Zagreb B",
                ARCH);

        t("Maven - The Hidden Gems",
                "Sandra Parsick",
                Talk.Format.SESSION, d06, h(16,45), h(17,45), "Kongresssaal",
                DEVOPS);

        t("Moderne Webanwendungen mit Java entwickeln – ganz ohne JavaScript",
                "Sebastian Kühnau",
                Talk.Format.SESSION, d06, h(18,15), h(19,15), "Gutenberg 1",
                SERVER_JAVA, WEB_JS, CORE_JAVA);

        t("Next Level Architecture: From Solution Architect to Enterprise Architect",
                "Karsten Voges",
                Talk.Format.SESSION, d06, h(18,15), h(19,15), "Gutenberg 2",
                ARCH);

        t("LLMs im Griff: Observability, Tracing und Security",
                "Torsten Bøgh Köster",
                Talk.Format.SESSION, d06, h(18,15), h(19,15), "Gutenberg 3",
                PERF_SEC, DATA_ML, GEN_AI);

        t("Things you can do with Spring Boot and Kotlin",
                "Frederik Pietzko",
                Talk.Format.SESSION, d06, h(18,15), h(19,15), "Zagreb A",
                SERVER_JAVA);

        t("Java im Zeitalter der Virtual Threads: Architektur, Performance und Structured Concurrency",
                "Marwan Abu-Khalil",
                Talk.Format.SESSION, d06, h(18,15), h(19,15), "Kongresssaal",
                PERF_SEC, CORE_JAVA);

        t("No Main Characters – How to Create Space for Introverts and Extroverts",
                "Samantha Casler, Sarah Rayfuse",
                Talk.Format.SESSION, d06, h(18,15), h(19,15), "Gutenberg 4",
                AGILE);

        t("Spec-driven Development mit KI",
                "Simon Martinelli",
                Talk.Format.SESSION, d06, h(18,15), h(19,15), "Watford",
                AGILE, GEN_AI);

        t("Die Rolle zwischen Entwickler und Architekt hat keinen Namen",
                "Sebastian Meyen, Lars Röwekamp",
                Talk.Format.PANEL, d06, h(19,20), h(20,5), "Center Stage",
                AGILE, ARCH, GEN_AI);

        t("Live Podcast: AIOps: Smarter Systems or Bigger Risks?",
                "Michael Dowden, Torsten Bøgh Köster, Christian Schneider",
                Talk.Format.SESSION, d06, h(20,15), h(21,15), "Center Stage",
                PERF_SEC, DEVOPS, ARCH, GEN_AI);

        t("Meilensteine und Kuriositäten des Software-Engineering",
                "Arno Haase",
                Talk.Format.SESSION, d06, h(20,15), h(21,15), "Gutenberg 1",
                AGILE);

        // ── 07 May 2026 ───────────────────────────────────────────────────────

        t("Java, AI Agents, and the Cloud-Native Shift: Architectures for the Next Generation of Intelligent Workloads",
                "Natale Vinto",
                Talk.Format.KEYNOTE, d07, h(9,0), h(9,30), "Kongresssaal",
                CLOUD, DATA_ML, GEN_AI);

        t("Kill the Vibe? Architektur im AI-Zeitalter",
                "Stefan Toth",
                Talk.Format.SESSION, d07, h(10,0), h(11,0), "Gutenberg 2",
                ARCH);

        t("Embrace AI Coding and Security Assistants While Efficiently Managing Risk",
                "Emmanuel Gonzalez Carmona",
                Talk.Format.SESSION, d07, h(10,0), h(11,0), "Gutenberg 4",
                PERF_SEC, GEN_AI);

        t("Embabel: Agentic AI planen statt prompten",
                "Patrick Baumgartner",
                Talk.Format.SESSION, d07, h(10,0), h(11,0), "Gutenberg 1",
                DATA_ML, GEN_AI);

        t("Authorization für APIs: JWT oder mTLS oder beides?",
                "Michael Hofmann",
                Talk.Format.SESSION, d07, h(10,0), h(11,0), "Watford",
                PERF_SEC, CLOUD, ARCH);

        t("Nur ein größeres Shopify? Lessons learned aus dem B2B-E-Commerce",
                "Silvia Schreier, Julian Herzog",
                Talk.Format.SESSION, d07, h(10,0), h(11,0), "Center Stage",
                ARCH, MICRO);

        t("So macht die Wartung von Maven-Projekten wieder Spaß - Werkzeuge und Good Practises",
                "Sandra Parsick",
                Talk.Format.SESSION, d07, h(10,0), h(11,0), "Kongresssaal",
                DEVOPS);

        t("Der Keycloak-Fehler, den 90 Prozent aller Entwickler:innen machen (und wie du ihn vermeidest)",
                "Niko Köbler",
                Talk.Format.SESSION, d07, h(10,0), h(11,0), "Gutenberg 3",
                PERF_SEC);

        t("Ressourceneffiziente KI",
                "Moritz Besser",
                Talk.Format.SHORTTALK, d07, h(11,5), h(11,25), "Center Stage",
                ARCH, GEN_AI);

        t("Is good architecture and clean code still relevant in the age of AI?",
                "Alexander von Zitzewitz",
                Talk.Format.SESSION, d07, h(11,30), h(12,30), "Watford",
                CORE_JAVA, GEN_AI);

        t("Cloud Native und Kubernetes: digitale Souveränität durch offene Infrastrukturen neu denken",
                "Peter Roßbach",
                Talk.Format.SESSION, d07, h(11,30), h(12,30), "Gutenberg 2",
                CLOUD);

        t("GitOps mit Kubernetes: Einführung und Praxis",
                "Thomas Kruse",
                Talk.Format.SESSION, d07, h(11,30), h(12,30), "Gutenberg 4",
                CLOUD, DEVOPS);

        t("Modularisierung pragmatisch: ein praktischer Deep Dive in Spring Modulith",
                "Nils Hartmann",
                Talk.Format.SESSION, d07, h(11,30), h(12,30), "Gutenberg 1",
                SERVER_JAVA, ARCH, MICRO);

        t("Chaos Meets Code: Fractals as a Playground for Modern Java Features",
                "Dr. Mihaela Gheorghe-Roman",
                Talk.Format.SESSION, d07, h(11,30), h(12,30), "Zagreb A",
                SERVER_JAVA, CORE_JAVA);

        t("Datenarchitektur: Das neue Rückgrat moderner Software?",
                "Gernot Starke, Simon Harrer",
                Talk.Format.SESSION, d07, h(11,30), h(12,30), "Kongresssaal",
                AGILE, ARCH, GEN_AI);

        t("Pimp Dein LLM! – Model Context Protocol mit Spring AI",
                "Kai Tödter",
                Talk.Format.SESSION, d07, h(11,30), h(12,30), "Gutenberg 3",
                ARCH, GEN_AI);

        t("Software Engineering at a Crossroads",
                "Sebastian Meyen, John Davies",
                Talk.Format.KEYNOTE, d07, h(14,0), h(14,45), "Kongresssaal",
                AGILE, GEN_AI);

        t("Von Chaos zu Kohäsion: Modulare Monolithen in der Praxis aufbauen",
                "Patrick Baumgartner",
                Talk.Format.SESSION, d07, h(15,15), h(16,15), "Gutenberg 3",
                ARCH, MICRO);

        t("Architectural Patterns for Spring Security You Wish Your Tech Lead Knew",
                "Dr. Cristian Schuszter",
                Talk.Format.SESSION, d07, h(15,15), h(16,15), "Gutenberg 1",
                SERVER_JAVA, PERF_SEC, ARCH);

        t("Agentic Engineering strategisch einführen – statt Tool-Stapeln",
                "Robert Glaser",
                Talk.Format.SESSION, d07, h(15,15), h(16,15), "Gutenberg 2",
                AGILE, GEN_AI);

        t("Mehr Sicherheit für APIs: Tokendiebstahl verhindern mit DPoP",
                "Niko Köbler",
                Talk.Format.SESSION, d07, h(15,15), h(16,15), "Gutenberg 4",
                PERF_SEC);

        t("Wie Java Speicher und Performance verschwendet",
                "Winfried Gerlach",
                Talk.Format.SESSION, d07, h(15,15), h(16,15), "Watford",
                PERF_SEC, CORE_JAVA);

        t("Digitale Souveränität ist keine Frage der Lizenz – es ist eine Frage der Kompetenz",
                "Björn Stahl",
                Talk.Format.SESSION, d07, h(15,15), h(16,15), "Zagreb A",
                AGILE, ARCH);

        t("Building Agentic AI with Small Language Models — Locally and Efficiently",
                "John Davies",
                Talk.Format.SESSION, d07, h(16,45), h(17,45), "Watford",
                DATA_ML, GEN_AI);

        t("Teststrategien für Hexagonale Architektur",
                "Andreas Jürgensen",
                Talk.Format.SESSION, d07, h(16,45), h(17,45), "Gutenberg 3",
                PERF_SEC, ARCH);

        t("Entwicklerpraxis im KI-Zeitalter: Training und Coaching von Developer Skills",
                "David Tanzer",
                Talk.Format.SESSION, d07, h(16,45), h(17,45), "Gutenberg 1",
                AGILE);

        t("Parallelität in Java: So wählst du das passende Paradigma für deine Anwendung",
                "Marwan Abu-Khalil",
                Talk.Format.SESSION, d07, h(16,45), h(17,45), "Gutenberg 2",
                PERF_SEC, CORE_JAVA);

        t("Soft Skills in the age of AI",
                "Kirill Boiarkin",
                Talk.Format.SESSION, d07, h(16,45), h(17,45), "Gutenberg 4",
                AGILE, DATA_ML, GEN_AI);

        t("Real-time Serverless APIs",
                "Michael Dowden",
                Talk.Format.SESSION, d07, h(16,45), h(17,45), "Zagreb A",
                CLOUD, WEB_JS, ARCH);

        // ── 08 May 2026 ───────────────────────────────────────────────────────

        t("Kubernetes-Schnellstart-Workshop",
                "Thomas Kruse",
                Talk.Format.WORKSHOP, d08, h(9,0), h(16,30), "Zagreb B",
                CLOUD);

        t("Workshop: Building Intelligent AI Agents with Open Source Models and the Model Context Protocol (MCP)",
                "John Davies",
                Talk.Format.WORKSHOP, d08, h(9,0), h(16,30), "Gutenberg 1",
                DATA_ML, GEN_AI);

        t("Test-driven Development Workshop – mit und ohne KI [Onsite Only]",
                "David Tanzer",
                Talk.Format.WORKSHOP, d08, h(9,0), h(16,30), "Dijon",
                PERF_SEC, AGILE);

        t("Architektur-Workshop: Architekt:in sein – mehr als Technik",
                "Eberhard Wolff",
                Talk.Format.WORKSHOP, d08, h(9,0), h(16,30), "Gutenberg 2",
                ARCH, MICRO);

        t("Java Virtual Threads Workshop: Performance und Skalierbarkeit in neuer Dimension",
                "Marwan Abu-Khalil",
                Talk.Format.WORKSHOP, d08, h(9,0), h(16,30), "Gutenberg 4",
                SERVER_JAVA, CORE_JAVA);

        t("Keycloak Quickstart Workshop",
                "Niko Köbler",
                Talk.Format.WORKSHOP, d08, h(9,0), h(16,30), "Zagreb A",
                PERF_SEC);

        t("Workshop: Monolithen modernisieren mit Spring Modulith",
                "Patrick Baumgartner",
                Talk.Format.WORKSHOP, d08, h(9,0), h(16,30), "Gutenberg 3",
                SERVER_JAVA);

        logger.info("create 149 talks");
    }

    private void t(String title, String speaker, Talk.Format format,
                   LocalDate date, LocalTime startTime, LocalTime endTime, String room,
                   Talk.Track... tracks) {
        Talk talk = new Talk();
        talk.setTitle(title);
        talk.setSpeaker(speaker);
        talk.setFormat(format);
        talk.setStartDate(date);
        talk.setStartTime(startTime);
        talk.setEndTime(endTime);
        talk.setRoom(room.trim());
        talk.setTracks(List.of(tracks));
        talkRepository.save(talk);
    }

    private static LocalTime h(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }
}
