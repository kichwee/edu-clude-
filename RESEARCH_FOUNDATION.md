# Education Cloud — Research Foundation V2
> OSINT-Backed Academic & Technical Deep-Dive
> Compiled: June 2026 | Revised after field intelligence

---

## Executive Summary

This document synthesizes PhD-level research, OSINT field intelligence, competitor analysis, and verified technical benchmarks to ground the **Education Cloud** project. The initial concept has been **pressure-tested and rebuilt** based on five critical findings:

1. **On-device AI is a feasible hypothesis** — quantized SLMs can potentially run on 2GB Android phones, but this has not been verified on a target low-end device in this project
2. **Feature phones need THREE channels, not one** — USSD (text menus), SMS (async lessons), and **Voice Calls** (real-time AI tutor via phone call)
3. **Voice-call AI tutoring is proven** — Yiya AirVoice in Uganda already does this; Africa's Talking Voice API provides the telephony layer
4. **You don't train a model — you fine-tune one** — specifically InkubaLM-0.4B or Phi-3 Mini
5. **Without curriculum grounding, the AI is useless** — Kenya CBC via KICD/OER is the content source

---

## 1. Competitor & Prior Art Analysis

### 1.1 FundAI (Direct Competitor)
- **What:** Offline AI tutors preloaded on laptops for African students
- **GitHub:** `EmmS21/fundAI` (stats as of June 2026: ~6 forks, ~215 commits, 10 releases — verify current numbers)
- **Stack:** llama-cpp + Groq API, PySide6 desktop apps, SQLite, MongoDB
- **Apps:** "The Engineer" (ages 12–18, software engineering), "The Examiner" (exam prep)
- **Gap:** Desktop-only (PySide6/Electron). No Android app. No USSD. No CBC alignment.
- **Takeaway:** Study their inference pipeline. Don't replicate their desktop-first mistake. Mobile-first is the correct entry point for Africa (smartphone penetration > laptop ownership).

### 1.2 PocketPal-AI (Fork Candidate — MIT Licensed)
- **What:** Open-source mobile app for running SLMs on iOS/Android
- **GitHub:** `a-ghorbani/pocketpal-ai` — MIT License
- **Stack:** React Native + `llama.rn` (llama.cpp JNI bindings)
- **Features:** GGUF model loading, offline inference, HuggingFace integration, benchmarking
- **Android Support:** NNAPI acceleration, runs on Android 8+
- **Why fork this:** It solves the hardest engineering problem (llama.cpp ↔ React Native bridge). We add education UI + RAG + CBC content on top.

### 1.3 Kolibri (Learning Equality)
- **What:** Open-source offline-first education platform (MIT License)
- **Deployed:** 200+ countries
- **Stack:** Django backend + Vue.js frontend
- **Gap:** **Zero AI capability.** Content-only platform with no tutoring intelligence.
- **Takeaway:** Their content pipeline (Kolibri Studio → channels → offline sync) is excellent. We should study their morango sync protocol for our sync layer.

### 1.4 Yiya AirScience (USSD Benchmark)
- **What:** USSD-based interactive STEM education in Uganda
- **Scale:** 53,000 learners, 1.25 million interactions in one year
- **Tech:** USSD menu trees + AirVoice (AI-powered phone call tutor)
- **Research Partner:** Carnegie Mellon University
- **Awards:** Innovation in Education in Africa Award, MIT Solve, African Union recognition
- **Takeaway:** Proves USSD education works at scale. Their AirVoice (cloud AI over phone call) is a model for our Track B.

### 1.5 M-Shule (Kenya — SMS Education)
- **What:** SMS-based adaptive learning for Kenyan primary students
- **Focus:** Literacy and numeracy on feature phones
- **Model:** AI selects next question based on previous answers (Item Response Theory)
- **Takeaway:** Proven in Kenya specifically. Their content chunking approach (SMS-sized lessons) is directly applicable.

### 1.6 Khanmigo (Khan Academy)
- **What:** GPT-4 powered AI tutor (cloud-only, US market)
- **Gap:** Requires internet, English-only, US curriculum, not open source
- **Takeaway:** Their Socratic prompt engineering is well-documented. Copy the pedagogy, not the architecture.

---

## 2. Model Selection — The Core Technical Decision

### 2.1 Primary Model: InkubaLM-0.4B (Lelapa AI)

| Attribute | Value |
|-----------|-------|
| Developer | Lelapa AI (South Africa) |
| Parameters | 400 million |
| Training tokens | 2.4 billion |
| Languages | isiZulu, Yoruba, Hausa, Swahili, isiXhosa, English, French |
| GGUF Available | Yes — `Skier8402/InkubaLM-0.4B-Q4_K_M-GGUF` on HuggingFace |
| Q4 Size | ~250–300 MB |
| RAM Required | ~1–1.5 GB |
| Key Advantage | **Fits on 2GB RAM phones. Speaks African languages natively.** |

**Why InkubaLM over Phi-3 or Llama:**
- Phi-3 Mini (3.8B) needs 2.2 GB just for the model. On a 2GB phone, that's OOM on launch.
- InkubaLM-0.4B Q4 fits in ~300MB. Leaves plenty of room for the app, RAG DB, and OS.
- InkubaLM was trained on Swahili and Hausa — our primary target languages for Kenya + West Africa.
- For CBC Kenya (Swahili + English bilingual instruction), this is the correct base model.

> [!WARNING]
> **2GB RAM Reality Check:** A 2GB Android device has only **500MB–1GB free RAM** after the OS and background processes. InkubaLM Q4 (~300MB) + app (~50MB) + RAG DB (~20MB) = ~370MB minimum. This is tight. Mandatory mitigations:
> 1. **Device profiling on first launch** — measure available RAM before loading model
> 2. **Lazy model loading** — load model only when inference is requested, unload after 60s idle
> 3. **Retrieval-only fallback** — if free RAM < 600MB, skip LLM and return RAG-retrieved textbook passages directly (no generation)
> 4. **Aggressive memory management** — call `gc()` after each inference, use mmap for model loading
> 5. **Target test devices** — Tecno Pop 7, Itel A18, Samsung Galaxy A04 (all 2GB, popular in Kenya)

### 2.2 Fallback Model: Phi-3 Mini 3.8B (4GB+ RAM devices)

| Attribute | Value |
|-----------|-------|
| Developer | Microsoft |
| Parameters | 3.8 billion |
| Q4 Size | ~2.2 GB |
| RAM Required | ~3.5–4 GB |
| Key Advantage | Stronger reasoning, better for math/science explanations |

**Use when:** Device has 4GB+ RAM (mid-range phones, tablets, laptops).

### 2.3 Why You Don't "Train" a Model

> [!CAUTION]
> **Training an LLM from scratch requires millions of dollars in compute.** GPT-4 cost an estimated $100M+ to train. Even a 400M param model from scratch costs $50K–$200K in cloud GPU time.

**What you actually do:**
1. Take InkubaLM-0.4B (already trained, free to download)
2. Fine-tune it on CBC curriculum data using **QLoRA** (4-bit quantized LoRA adapters)
3. This costs ~$20 on a cloud GPU (Google Colab Pro) and takes 4–8 hours
4. Export the fine-tuned model as GGUF → ship it in the app

**Research Citations:**
- Dettmers et al. (2023). "QLoRA: Efficient Finetuning of Quantized LLMs." NeurIPS 2023.
- Hu et al. (2022). "LoRA: Low-Rank Adaptation of Large Language Models." ICLR 2022.

---

## 3. On-Device RAG Architecture

### 3.1 Why RAG is Non-Negotiable

Without RAG, the AI hallucinates. It invents plausible-sounding but wrong answers to curriculum questions. Teachers reject it. Ministry doesn't endorse it. **Adoption = 0.**

RAG ensures:
- Every answer is grounded in real CBC curriculum content
- The model retrieves relevant textbook passages before answering
- Answers cite the source ("As explained in Grade 7 Science, Chapter 4...")
- Factual accuracy is bounded by the quality of the embedded content

### 3.2 Technical Implementation

```
Student asks question
    ↓
Embed question locally (multilingual-e5-small, ~118MB, supports Swahili)
    ↓
Cosine similarity search in sqlite-vec (local SQLite + brute-force scan)
    ↓
Top-3 curriculum chunks retrieved
    ↓
Chunks injected into SLM prompt as context
    ↓
InkubaLM-0.4B generates answer grounded in curriculum
    ↓
Answer displayed to student
```

> [!IMPORTANT]
> **Embedding Model Selection:** `all-MiniLM-L6-v2` and `nomic-embed-text` are English-optimized. For Swahili RAG queries, use **`multilingual-e5-small`** (~118MB) or **`paraphrase-multilingual-MiniLM-L12-v2`** (~470MB). The embedding model directly determines retrieval accuracy — benchmark against Swahili test queries before shipping.

### 3.3 sqlite-vec (Recommended Vector Store)

| Attribute | Value |
|-----------|-------|
| Type | SQLite loadable extension (C, no dependencies) |
| Platform | Android, iOS, Linux, Windows, macOS |
| Vector Format | BLOB columns (float32 arrays) |
| Indexing | **Exact brute-force scan** with optional TurboQuant (2/3/4-bit vector quantization for speed) |
| SIMD | ARM NEON acceleration (requires custom compilation with NEON flags for Android ARM targets) |
| Memory | ~30MB default footprint |
| Alternative | ObjectBox (built-in HNSW for approximate search, heavier dependency) |

> [!NOTE]
> sqlite-vec does **not** use HNSW (Hierarchical Navigable Small World) indexing. It uses exact brute-force cosine similarity search, which is perfectly fast for our dataset size (~5000 vectors = <10ms query time). HNSW is only available via the ObjectBox alternative.

### 3.4 Storage Math

Each 384-dimensional embedding vector (multilingual-e5-small) = 1.5KB.
A full K-6 CBC curriculum (~1000 pages) chunked into ~5000 passages:
- Embeddings: 5000 × 1.5KB = **7.5MB**
- Text chunks: ~5MB
- Embedding model: ~118MB
- **Total RAG footprint: ~130MB**

**Total APK bundle estimate:**
- InkubaLM-0.4B Q4_K_M: ~300MB
- Embedding model: ~118MB
- RAG database: ~13MB
- App code + assets: ~40MB
- **Total: ~470MB** (within 500MB ceiling, but tight — consider split APK + model download on first launch)

---

## 4. Kenya Curriculum Data Sources

### 4.1 Official KICD Sources

| Source | URL | Content |
|--------|-----|---------|
| Kenya Education Cloud (KEC) | kec.ac.ke | Interactive digital content, digitized textbooks, OERs |
| KICD Official | kicd.ac.ke | Curriculum designs (PDF), approved materials lists |
| OER Portal | via KEC | Open-licensed educational materials |

### 4.2 CBC Structure (2-6-3-3-3 Model)

```
Pre-Primary (PP1–PP2): Ages 4–5
Primary (Grade 1–6): Ages 6–11 ← START HERE
Junior Secondary (Grade 7–9): Ages 12–14
Senior Secondary (Grade 10–12): Ages 15–17
```

**Why start with Primary (Grade 1–6):**
- Highest impact (foundational literacy + numeracy)
- Simpler content (less risk of AI errors)
- Largest student population
- Least access to qualified teachers in marginalized areas

### 4.3 Open Content Pipeline

1. KICD Curriculum Design PDFs (public, available on kicd.ac.ke)
2. CK-12 Africa-aligned content (CC-BY licensed)
3. OpenStax textbooks (CC-BY licensed)
4. African Storybook Initiative (local language stories, CC licensed)
5. Wikipedia offline dump (Kiwix format, CC-BY-SA)

---

## 5. Feature Phone Architecture (Track B) — Three Channels

### 5.1 The Fundamental Architecture Split

> [!WARNING]
> **You cannot run llama.cpp on a Nokia feature phone.** Feature phones have ~2MB RAM, no application layer, and communicate via SMS/USSD only. The architecture for feature phones is fundamentally cloud-connected.

**Track A (Android, on-device):** AI runs on the phone. Zero internet after setup.
**Track B (Feature phones, cloud-backed):** AI runs on a server. The phone is just an I/O device.

Track B has **five channels**, each suited to different interaction patterns and phone capabilities:

```
Track B: Feature Phone / Button Phone Channels
├── B1: USSD (*384#)          → Text menus, quizzes, micro-lessons (synchronous)
├── B2: SMS                   → Async lesson delivery, homework, progress reports
├── B3: VOICE CALL            → Real-time AI tutor conversation via phone call
│                                Student dials a number → talks to AI 1-on-1
│                                THE MOST POWERFUL CHANNEL FOR NON-LITERATE LEARNERS
├── B4: SIM TOOLKIT (STK)     → Menu-based app embedded on the SIM card itself
│                                Works on ANY phone with a SIM. No internet needed.
│                                M-Pesa started as STK. Proven at 50M+ users.
└── B5: IVR PRE-RECORDED      → Dial-in lesson library via phone keypad
                                 Student calls → navigates lesson menu with buttons
                                 NO AI processing — pure audio playback. Cheapest.
```

> [!IMPORTANT]
> **Voice calls eliminate the literacy barrier.** A student who cannot read can still call a number and have a spoken conversation with an AI tutor. This is the most inclusive channel.

### 5.2 Channel B1: USSD Technical Architecture

```
Student dials *384#
    ↓
GSM Network routes to USSD Gateway (Africa's Talking)
    ↓
Gateway sends HTTP POST to Django backend (cloud VPS)
    ↓
Django processes: session state (Redis), user progress (PostgreSQL)
    ↓
For AI answers: calls Gemini/Claude API (cloud inference)
    ↓
Text response (≤182 chars) sent back through gateway
    ↓
Student sees response on feature phone screen
```

### 5.3 Channel B2: SMS Architecture

```
Student sends SMS to shortcode (e.g., 40384)
    ↓
Africa's Talking / Twilio SMS API receives message
    ↓
Django backend processes: parse question, check progress
    ↓
LLM generates response (Gemini Flash — cheap, fast)
    ↓
Response sent back as SMS (160 chars) or multi-part SMS
    ↓
Async: scheduled daily lessons sent to student at set time
```

**SMS Advantages over USSD:**
- No session timeout (USSD has 180-second limit)
- Can send longer content (multi-part SMS)
- Can schedule daily lesson delivery (push, not pull)
- Student can reply at their own pace
- Works even when GSM signal is weak (store-and-forward)

### 5.4 Channel B3: VOICE CALL — Real-Time AI Tutor

This is the breakthrough channel. The student dials a phone number and has a **spoken conversation with an AI tutor**. The phone acts purely as a microphone and speaker — all intelligence is in the cloud.

**Proven by:** Yiya AirVoice (Uganda) — AI-powered voice tutor that students call from any basic phone.

#### 5.4.1 Voice Call Architecture

```
Student dials +254-XXX-XXXX (Education Cloud Voice Line)
    ↓
Africa's Talking Voice API receives call
    ↓
AT sends HTTP POST to our webhook: /voice-callback
    ↓
┌─────────────────────────────────────────────────────┐
│  VOICE AI PIPELINE (on our cloud server)            │
│                                                     │
│  1. GREETING                                        │
│     AT <Say> action: "Karibu Education Cloud!       │
│     Ni somo gani unahitaji msaada?"                 │
│     (Welcome! What subject do you need help with?)  │
│                                                     │
│  2. RECORD student speech                           │
│     AT <Record> action → captures audio chunk       │
│     OR: <GetDigits> for DTMF menu (press 1,2,3)    │
│                                                     │
│  3. SPEECH-TO-TEXT (STT)                             │
│     Recorded audio → Whisper (fine-tuned for        │
│     Swahili/African accents) → text transcript      │
│                                                     │
│  4. AI PROCESSING                                   │
│     Transcript → Gemini Flash (with CBC RAG          │
│     context injected) → generates tutoring response │
│                                                     │
│  5. TEXT-TO-SPEECH (TTS)                             │
│     AI response text → Kokoro (Apache 2.0) or       │
│     Piper (MIT) for Swahili → audio                 │
│                                                     │
│  6. PLAY response                                   │
│     AT <Play> or <Say> action → student hears       │
│     AI tutor speaking the answer                    │
│                                                     │
│  7. LOOP: Record next question → process → respond  │
│     Continue until student hangs up                  │
│     Session state stored in Redis per sessionId     │
└─────────────────────────────────────────────────────┘
```

#### 5.4.2 Voice Pipeline Technical Stack

| Component | Technology | Notes |
|-----------|-----------|-------|
| **Telephony** | Africa's Talking Voice API | Pan-African MNO coverage, XML-based call control |
| **Webhook Server** | Django + Django REST Framework | Handles AT callbacks, manages session state |
| **STT (Speech-to-Text)** | Whisper Large v3 (fine-tuned for Swahili) | Or: `intronhealth/afrispeech-whisper-medium-all` for African accents |
| **LLM (Brain)** | Gemini Flash (latest) | Cheapest cloud LLM with strong multilingual. CBC RAG context injected. Architect for swappable providers. |
| **TTS (Text-to-Speech)** | **Kokoro** (Apache 2.0, cloud) or **Piper** (MIT, on-device/hub) | ~~Coqui XTTSv2~~ non-commercial license. Kokoro/Piper are commercially safe. Fine-tune on WAXAL data for Swahili. |
| **Session State** | Redis | Stores conversation history per `sessionId` |
| **User Progress** | PostgreSQL | Same DB as USSD/SMS tracks |
| **Audio Codec** | G.711 µ-law (8kHz) | Standard phone audio. Must transcode to 16kHz for Whisper. |

#### 5.4.3 Alternative: Hybrid DTMF + Voice

For areas with poor audio quality or heavy accents that challenge STT:

```
Student calls → hears menu:
"Press 1 for Hesabu (Math)
 Press 2 for Sayansi (Science)
 Press 3 to ask a question"
    ↓
If Press 1 or 2: Pre-recorded audio lessons play
If Press 3: Live STT → LLM → TTS conversation
```

This hybrid approach reduces STT dependency by using DTMF (keypad) for navigation and only activating voice AI for free-form questions.

#### 5.4.4 Voice Call Constraints & Solutions

| Constraint | Solution |
|-----------|----------|
| Telephony costs ($0.02–0.05/min) | Donor subsidization, government partnership, or toll-free number |
| STT accuracy for Swahili | Fine-tune Whisper on AfriSpeech + WAXAL datasets |
| Audio latency (STT → LLM → TTS) | Use Gemini Flash latest (<1s inference); stream TTS; target <3s total latency. Architect for swappable LLM providers (Gemini/Claude/local). |
| Background noise on calls | VAD (Voice Activity Detection) filtering; Whisper handles noise well |
| Session state on dropped calls | Redis + PostgreSQL persistence; student redials → resumes conversation |
| Non-literate students | Voice is the only channel that fully works for them |

#### 5.4.5 STT/TTS for African Languages — Current State

**Speech-to-Text (Whisper ecosystem):**
- OpenAI Whisper Large v3: supports Swahili, Hausa natively (trained on diverse audio)
- `intronhealth/afrispeech-whisper-medium-all`: fine-tuned on AfriSpeech-200 dataset for African accents
- **WAXAL dataset (2026)**: 11,000+ hours of speech across 21 Sub-Saharan African languages (Swahili, Hausa, Yoruba, Igbo, Luganda, etc.) — critical for fine-tuning
- `African Whisper` project: framework for fine-tuning Whisper on Common Voice + FLEURS for African languages

**Text-to-Speech:**

> [!CAUTION]
> **Coqui AI shut down in December 2023.** The XTTS-v2 *model weights* are under the **Coqui Public Model License (CPML) — non-commercial use only.** The code framework (MPL 2.0) is maintained by the community (Idiap Research Institute), but you **cannot use XTTS-v2 weights in a commercial product** without licensing that no longer exists. Use commercially-licensed alternatives below.

- ~~Coqui XTTSv2~~ — **NOT recommended for commercial use** (CPML non-commercial license)
- **Piper** (MIT License): fast CPU-based TTS, runs on Raspberry Pi, many languages, community Swahili models. **Recommended for School Hub (Idea 4).**
- **Kokoro** (Apache 2.0): lightweight, modern, high-quality. **Recommended for cloud Voice pipeline.**
- **Chatterbox** (MIT License): production-grade, 23+ languages, strong voice cloning
- **WAXAL TTS data**: 20+ hours of studio-quality recordings per language, designed for TTS training
- **Google Cloud TTS**: supports Swahili (sw-KE), but costs money per request ($4/1M chars)
- **ElevenLabs**: excellent quality but expensive; not recommended for scale
- `Msingi-AI/Sauti`: community fine-tuned Swahili TTS model on HuggingFace (non-commercial research)

### 5.5 USSD Constraints & Solutions

| Constraint | Solution |
|-----------|----------|
| 182 chars per screen | Micro-learning: chunk content to 3–5 screens |
| 180-second session timeout | Save progress per interaction, resume next dial |
| Text only (no images) | Structured quiz format (A/B/C/D) |
| No on-device AI | Pre-computed adaptive question banks + cloud API for free-form |
| Cost per session (~$0.01) | M-Pesa micro-payments or donor subsidization |

### 5.6 Proven Feature Phone Education Platforms

| Platform | Country | Scale | Channels |
|----------|---------|-------|----------|
| Yiya AirScience | Uganda | 53K learners, 1.25M interactions | USSD + **Voice (AirVoice)** |
| M-Shule | Kenya | At scale | SMS adaptive learning |
| Eneza Education | Kenya | 6M+ users | SMS + USSD |
| Africa's Talking | Pan-African | Infrastructure | USSD + SMS + **Voice** API |

---

## 6. Distribution & Cold-Start Problem

### 6.1 The Core Problem

Only 36% of Africans have internet access. The app is ~500MB (APK + model + RAG DB). How does it get onto the device?

### 6.2 Distribution Strategies

| Strategy | How | Cost |
|----------|-----|------|
| School IT Lab | Teacher downloads once, sideloads to students via USB/WiFi Direct | Free after initial download |
| SD Card Kit | Pre-loaded SD cards distributed to schools | ~$2–5 per card |
| Community Hotspot | Raspberry Pi with content, students download via local WiFi | ~$60 per hotspot |
| M-Pesa Integration | Students pay small fee, receive download link via SMS | Self-sustaining |
| Pre-loaded Devices | Partner with phone manufacturers to pre-install | Requires business deal |
| APK + Model Bundle | Single ZIP file, distributable via any transfer method | ~500MB |

### 6.3 Offline Update Mechanism

1. New curriculum content / model adapter released
2. Distributed to school IT labs or community hotspots
3. Teacher's device syncs via WiFi Direct
4. Students sync from teacher's device
5. Or: new SD card distributed quarterly

---

## 7. Failure Scenarios & Mitigations

| Scenario | Risk | Mitigation |
|----------|------|------------|
| **Generic chatbot trap** | AI gives non-CBC answers | RAG-grounded answers only; systematic curriculum alignment testing |
| **Device fragmentation** | OOM on 2GB phones | InkubaLM-0.4B (300MB) instead of Phi-3 (2.2GB); device-aware model selection; retrieval-only fallback |
| **Model distribution wall** | Can't download 2GB offline | Bundle model in APK (<500MB total); SD card distribution; split APK strategy |
| **Content misalignment** | AI contradicts CBC teaching | RAG with KICD-sourced content; teacher review before deployment |
| **Battery/thermal** | Inference drains battery | Throttle inference; limit context window; low-power mode; lazy model unloading |
| **Teacher resistance** | Teachers see AI as replacement | Position as "teaching assistant"; teacher dashboard; teacher-controlled |
| **Privacy breach** | Student data exposed | On-device encryption (AES-256); DPA 2019 compliance; parental consent flow |
| **Voice abuse** | Spam/prank calls to voice line | Rate limiting per phone number; DTMF CAPTCHA; caller ID verification |
| **Content moderation** | Student prompts inappropriate content | Output filtering; topic restriction to CBC subjects; blocked keyword list |
| **MNO outage** | Africa's Talking goes down | Multi-provider failover (Twilio backup); graceful degradation to pre-recorded content |

---

## 8. Academic References

### On-Device LLM & Quantization
1. Dettmers, T., et al. (2023). "QLoRA: Efficient Finetuning of Quantized LLMs." NeurIPS 2023.
2. Hu, E., et al. (2022). "LoRA: Low-Rank Adaptation of Large Language Models." ICLR 2022.
3. Frantar, E., et al. (2023). "GPTQ: Accurate Post-Training Quantization for Generative Pre-trained Transformers." ICLR 2023.
4. Xu, Y., et al. (2024). "On-Device Language Models: A Comprehensive Review." arXiv:2409.00088.

### African Language Models
5. Oladipo, A., et al. (2023). "InkubaLM: A small language model for low-resource African languages." Lelapa AI / arXiv.
6. Adelani, D., et al. (2022). "A Few Thousand Translations Go a Long Way! Leveraging Pre-trained Models for African Languages." NAACL 2022.

### Intelligent Tutoring Systems
7. Bloom, B. S. (1984). "The 2 Sigma Problem." Educational Researcher, 13(6), 4–16.
8. VanLehn, K. (2011). "The Relative Effectiveness of Human Tutoring, Intelligent Tutoring Systems, and Other Tutoring Systems." Educational Psychologist, 46(4).

### Federated Learning
9. McMahan, H. B., et al. (2017). "Communication-Efficient Learning of Deep Networks from Decentralized Data." AISTATS 2017.
10. Kairouz, P., et al. (2021). "Advances and Open Problems in Federated Learning." Foundations and Trends in ML.

### Education in Developing Contexts
11. UNESCO (2023). "Technology in Education: A Tool on Whose Terms?" GEM Report 2023.
12. UNESCO (2016). "If You Don't Understand, How Can You Learn?" Policy Paper 24.
13. World Bank (2022). "Closing the Gaps: Digital Divide and Inequality in Access to Educational Technology."
14. Carnegie Mellon / Yiya Solutions (2023). "Evaluating USSD-based STEM Learning in Rural Uganda."

### Voice AI & African Language Speech
15. Radford, A., et al. (2023). "Robust Speech Recognition via Large-Scale Weak Supervision." (Whisper). ICML 2023.
16. WAXAL Consortium (2026). "WAXAL: An Open-Access Speech Dataset for 21 Sub-Saharan African Languages." 11,000+ hours.
17. IntronHealth (2024). "AfriSpeech-200: Pan-African Accented Speech Dataset." HuggingFace.
18. Casanova, E., et al. (2024). "XTTS: A Massively Multilingual Zero-Shot Text-to-Speech Model." (Coqui XTTSv2).

### Platform References
19. FundAI: github.com/EmmS21/fundAI
20. PocketPal-AI: github.com/a-ghorbani/pocketpal-ai (MIT License)
21. Kolibri: github.com/learningequality/kolibri (MIT License)
22. sqlite-vec: github.com/asg017/sqlite-vec
23. llama.cpp: github.com/ggerganov/llama.cpp
24. InkubaLM GGUF: huggingface.co/Skier8402/InkubaLM-0.4B-Q4_K_M-GGUF
25. ~~Coqui TTS: github.com/coqui-ai/TTS~~ (Company shut down Dec 2023; XTTS-v2 model is CPML non-commercial)
25b. Piper TTS: github.com/rhasspy/piper (MIT License) — **Recommended replacement**
25c. Kokoro TTS: (Apache 2.0) — **Recommended for cloud Voice pipeline**
26. Africa's Talking Voice API: developers.africastalking.com/docs/voice
27. Yiya AirVoice: yiyasolutions.org
28. Msingi-AI/Sauti (Swahili TTS): huggingface.co/Msingi-AI/Sauti

---

## 9. Legal & Compliance Framework

> [!CAUTION]
> Education Cloud processes personal data of **children (ages 4–17)** in Kenya. This triggers mandatory compliance under the **Kenya Data Protection Act 2019 (DPA)** — Kenya's equivalent to GDPR, with specific heightened protections for children. Non-compliance carries significant penalties enforced by the Office of the Data Protection Commissioner (ODPC).

### 9.1 Mandatory Requirements

| Requirement | Implementation |
|-------------|----------------|
| **Parental/Guardian Consent** | Verifiable consent before any data collection. For Track B (SMS/USSD): parent sends opt-in SMS. For Track A: in-app consent flow on first launch with parent's phone number verification. |
| **Best Interests of the Child** | All data processing must protect and promote child's rights. AI outputs filtered for age-appropriateness. No behavioral profiling beyond educational adaptation. |
| **Purpose Limitation** | Data collected ONLY for educational tutoring. No advertising. No selling data. No secondary use. |
| **Data Minimization** | Collect only: name/alias, grade, subject performance, device ID. No location, no photos, no biometrics. |
| **Privacy by Design** | Encryption at rest (AES-256 on local SQLite). Track A data stays on-device by default. Track B data encrypted in transit (TLS 1.3) and at rest. |
| **Data Protection Impact Assessment** | DPIA required before launch — document risks to children's privacy and mitigations. |
| **Data Protection Officer** | Appoint DPO before pilot launch. Can be part-time for early stage. |
| **Right to Erasure** | Parent can request deletion of child's data via SMS command or in-app. System must purge within 30 days. |
| **Data Retention** | Student data retained for active school year + 1 year. Auto-purge after 2 years of inactivity. |
| **Cross-border Transfer** | If cloud servers are outside Kenya (AWS/GCP), implement Standard Contractual Clauses (SCCs). |

### 9.2 KICD Content Licensing

Before embedding KICD curriculum content:
1. Verify each source's license (OER vs. copyrighted)
2. CK-12 content is CC-BY — permitted with attribution
3. OpenStax is CC-BY — permitted with attribution
4. KICD's own PDFs may be Crown Copyright — **obtain written permission from KICD before embedding**
5. African Storybook Initiative — CC licensed, verify specific CC variant per story

### 9.3 AI-Specific Compliance

- **Transparency:** Students and parents must know they are interacting with AI, not a human teacher
- **No autonomous decisions:** AI does not make decisions about student advancement or grading for official records
- **Audit trail:** All AI-generated assessment results logged with timestamp and RAG context used
- **Bias monitoring:** Quarterly review of AI outputs across gender, region, and language for systematic bias

---

## 10. Security Architecture

### 10.1 Track A (On-Device) Security

| Layer | Protection |
|-------|------------|
| **Local Database** | SQLite with SQLCipher encryption (AES-256-CBC). Encryption key derived from device-specific hardware ID + user PIN. |
| **Model Files** | Read-only, integrity-verified via SHA-256 hash on launch. Tamper detection triggers re-download. |
| **App Storage** | Android Keystore for sensitive tokens. No plaintext credentials. |
| **Data at Rest** | Student performance data encrypted. Student name stored as pseudonym option available. |

### 10.2 Track B (Cloud) Security

| Layer | Protection |
|-------|------------|
| **API Authentication** | USSD/SMS: session-based via Africa's Talking callback verification (IP allowlist + API key). Voice: AT webhook signature validation. |
| **Rate Limiting** | Per-phone-number: max 50 USSD sessions/day, max 10 voice calls/day, max 100 SMS/day. |
| **Abuse Prevention** | DTMF CAPTCHA for voice before connecting to AI. SMS keyword blocklist. Progressive backoff for rapid-fire requests. |
| **Content Moderation** | Output filtered against blocked topic list (violence, sexual content, hate speech). Topics restricted to CBC curriculum subjects only. |
| **Infrastructure** | TLS 1.3 for all API calls. Database credentials in environment variables, never in code. Redis AUTH enabled. |
| **Admin Access** | Role-based: Super Admin, School Admin, Teacher. MFA required for Super Admin. |

### 10.3 Content Safety for AI Outputs

```
Student Input → Keyword Filter → LLM Generation → Output Filter → Response
                    ↓                                    ↓
              Block if off-topic                  Block if harmful
              Log + alert                         Replace with safe response
```

---

## 11. Infrastructure Architecture

### 11.1 Track B Cloud Deployment

```
┌─────────────────────────────────────────────────────┐
│  PRODUCTION INFRASTRUCTURE (Track B)                │
│                                                     │
│  Load Balancer (Nginx / Caddy)                      │
│       ↓                                             │
│  App Servers (2x Django + Gunicorn + Uvicorn)       │
│       ├── USSD webhook handlers                     │
│       ├── SMS webhook handlers                      │
│       └── Voice callback handlers                   │
│       ↓                                             │
│  Redis Cluster (session state, rate limiting)        │
│       ↓                                             │
│  PostgreSQL (user progress, analytics)              │
│       ↓                                             │
│  Whisper Server (GPU, STT processing)               │
│       ↓                                             │
│  TTS Server (Kokoro/Piper, audio generation)        │
│       ↓                                             │
│  LLM API (Gemini Flash via API)                     │
└─────────────────────────────────────────────────────┘

Hosting Options (by priority):
1. Hetzner Cloud (Nairobi proximity, EU GDPR, ~$50/mo for 2 app servers)
2. DigitalOcean (Singapore/Amsterdam, ~$80/mo)
3. AWS Africa (Cape Town, af-south-1, ~$120/mo but lowest latency)
```

### 11.2 Scaling Strategy

| Scale | Infrastructure | Cost Estimate |
|-------|---------------|---------------|
| 0–1K users | Single VPS (4 vCPU, 8GB RAM) + managed PostgreSQL | ~$50/mo |
| 1K–10K users | 2 app servers behind load balancer + Redis cluster | ~$150/mo |
| 10K–100K users | Kubernetes cluster (3 nodes) + dedicated GPU for Whisper | ~$500/mo |
| 100K+ users | Multi-region + CDN + auto-scaling + dedicated Whisper fleet | ~$2,000/mo |

### 11.3 Disaster Recovery

- **Database:** Daily automated PostgreSQL backups to object storage (30-day retention)
- **Redis:** Redis persistence (RDB snapshots every 15 min + AOF)
- **Multi-provider failover:** If Africa's Talking has outage, switch to Twilio for SMS/Voice
- **Runbook:** Document procedures for top 5 incident types (server down, DB corruption, AT outage, LLM quota exceeded, voice quality degradation)

---

## 12. Offline-to-Online Sync Architecture

### 12.1 What Syncs vs. What Stays Local

| Data | Direction | Frequency |
|------|-----------|-----------|
| Student performance scores | Device → Cloud | When connectivity available (opportunistic) |
| Weak spot reports (aggregated) | Device → Cloud | Weekly (for parent reports) |
| New curriculum content | Cloud → Device | Quarterly (via hub or zero-rated endpoint) |
| Model adapter updates (LoRA) | Cloud → Device | Quarterly |
| RAG DB patches | Cloud → Device | As needed |
| Student chat history | **NEVER syncs** | Stays on device permanently |
| Raw student answers | **NEVER syncs** | Stays on device permanently |

### 12.2 Conflict Resolution

- **Strategy:** Last-write-wins with device-local timestamp
- **Rationale:** Student progress is append-only (new quiz results, new scores). There are no concurrent writes to the same record from multiple devices.
- **Edge case:** Student uses two devices (e.g., phone at home, school tablet) → merge by taking the higher score per topic (optimistic merge)

### 12.3 Sync Protocol

- Lightweight JSON payloads (<50KB per sync)
- HTTPS POST to `/api/v1/sync` endpoint
- Device sends: `{device_id, student_id, scores[], last_sync_timestamp}`
- Server responds: `{new_content_available: bool, content_url, adapter_url}`
- Retry with exponential backoff (1s, 2s, 4s, 8s, max 60s)

---

## 13. Monitoring & Observability

### 13.1 Track A (Offline Devices)

- **Crash reporting:** Bundle a lightweight crash reporter that queues crash logs locally. On next sync (hub WiFi or zero-rated endpoint), upload crash logs.
- **Usage analytics:** Aggregate daily: sessions_count, questions_asked, subjects_used, avg_inference_time_ms. Sync when connected.
- **OOM tracking:** Log every OOM event with device model, RAM available, and model loaded.
- **No real-time monitoring possible** — accept this constraint. Review aggregated data weekly.

### 13.2 Track B (Cloud)

| Metric | Tool | Alert Threshold |
|--------|------|-----------------|
| API response time (P95) | Prometheus + Grafana | > 3s for USSD, > 5s for Voice |
| Error rate | Prometheus | > 5% of requests |
| Voice call drop rate | Custom logging | > 10% of calls |
| STT accuracy (spot-check) | Manual review queue | < 70% word accuracy |
| Redis memory usage | Prometheus | > 80% capacity |
| PostgreSQL connections | Prometheus | > 80% pool |
| Gemini API quota remaining | Custom check | < 20% of daily quota |

### 13.3 SLA Targets

| Channel | Availability Target | Rationale |
|---------|---------------------|----------|
| USSD | 99.5% | Students expect it to work like M-Pesa |
| SMS | 99.9% | Store-and-forward, inherently reliable |
| Voice | 99.0% | More complex pipeline, higher tolerance for downtime |
| Track A (offline) | 100% on-device | No server dependency = always available |

---

## 14. Content Management Pipeline

### 14.1 Content Creation Workflow

```
KICD Curriculum Design (PDF)
    ↓
Content Team digitizes → structured Markdown/JSON
    ↓
Subject Matter Expert (teacher) reviews for accuracy
    ↓
Chunked into RAG passages (200-500 words each)
    ↓
Embedded with multilingual-e5-small
    ↓
Loaded into sqlite-vec database
    ↓
Version tagged (e.g., CBC-2026-Q3)
    ↓
Distributed via hub sync / SD card / zero-rated endpoint
```

### 14.2 Content Versioning

- Every RAG database release tagged: `cbc-content-v{MAJOR}.{MINOR}`
- Major version: new grade level or subject added
- Minor version: corrections, additions within existing subjects
- Devices store current version locally, check for updates on sync

### 14.3 Quality Assurance

- **Automated testing:** 500+ test questions per subject with expected RAG retrieval targets. CI pipeline checks retrieval accuracy > 85%.
- **Teacher review board:** 5–10 volunteer teachers review AI responses monthly. Flag inaccurate answers.
- **Hallucination detection:** Compare AI answer against retrieved RAG chunks. If answer contains claims not in retrieved context, flag for review.

---

*Last Updated: June 13, 2026 | Education Cloud V2 — Post-OSINT Rebuild*
