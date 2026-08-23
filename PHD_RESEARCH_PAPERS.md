# Education Cloud — PhD Research Papers & Academic Foundation
> Compiled: July 2026 | Scope: 10 Technical Domains | 31+ Key Papers
> Synthesized to inform every layer of the Education Cloud stack

---

## How to Read This Document

Each paper is tagged with a **domain**, **relevance tier**, and a **"Build Impact"** note explaining how it shapes a design decision in the project.

| Relevance Tier | Meaning |
|---|---|
| P0 — Must Read | Foundational; directly validates a core technical decision |
| P1 — Important | Strongly informs design choices |
| P2 — Reference | Useful context; cite in grant proposals / KICD submissions |

---

## Domain 1: Pedagogical Foundation — Why AI Tutoring Works

### [P0] Bloom, B.S. (1984). "The 2 Sigma Problem"
**Journal:** Educational Researcher, 13(6), 4-16.
**Summary:** Students receiving one-on-one human tutoring + mastery learning scored 2 standard deviations (2σ) better than conventional classroom students. With 1 teacher per 60 students in rural Kenya, this is the scientific justification for Education Cloud.
**Build Impact:**
- This is the "why" behind every PRD feature. One-on-one tutoring = our AI goal. Mastery learning = spaced review engine.
- **Important caveat:** Bloom studied *human* tutoring, not AI. VanLehn (2011) shows ITS achieve d≈0.76 (roughly 1σ), not 2σ. Do not claim EduCloud equals a human tutor — claim it is *inspired by* Bloom's finding and aims to close the gap.
- Use in stakeholder presentations with this nuance. The "tutoring gap" is the market opportunity Education Cloud addresses.

---

### [P0] VanLehn, K. (2011). "The Relative Effectiveness of Human Tutoring, Intelligent Tutoring Systems..."
**Journal:** Educational Psychologist, 46(4), 197-221.
**Summary:** Meta-analysis showing ITS achieve effect size d=0.76, comparable to human tutors (d=0.79). ITS works best with step-level feedback, not just final-answer checking.
**Build Impact:**
- Validates building a software tutor (Track A + B) as scientifically sound.
- Validates step-by-step Socratic prompting in the LLM system prompt.
- Effect size justifies the "15% pre/post test improvement" pilot success criterion (PRD Section 7).

---

### [P1] Kulik, J.A. & Fletcher, J.D. (2016). "Effectiveness of Intelligent Tutoring Systems: A Meta-Analytic Review."
**Journal:** Review of Educational Research, 86(1), 42-78.
**Summary:** 50+ ITS studies show consistent positive effects (d=0.3-0.4) vs. conventional instruction. Domain specificity and feedback quality are the two strongest predictors of ITS success.
**Build Impact:**
- Validates 85% RAG accuracy target (PRD Section 7).
- Confirms domain-specific grounding (CBC curriculum) matters more than model size — supports RAG-first architecture.
- Informs "teacher NPS > 40" metric: teacher buy-in is the strongest external predictor of ITS adoption.

---

### [P1] Socratic AI Tutoring — arXiv Papers (2024)
**Key Papers:**
- "SocraticAI: Toward Socratic Dialogue for Computer Science Education" (arXiv, 2024)
- "From Answer-Providing to Scaffolding: Socratic LLM Tutors" (arXiv, 2024)
**Summary:** RCTs show Socratic scaffolding produces 5.5 percentage point improvement on novel problem solving vs. direct answer-giving. Direct answer-giving can harm long-term learning.
**Build Impact:**
- Directly shapes the LLM system prompt. Do NOT design AI to give answers directly.
  - BAD: "The answer is 15 because..."
  - GOOD: "Good thinking! Now, what do we need to add next to reach the answer?"
- Implement Socratic system prompt in backend/prompts/socratic_tutor_sw.txt and backend/prompts/socratic_tutor_en.txt.
- Add "patience mode" (no timer, repeat explanations) as an accessibility feature.

---

### [P2] UNESCO (2016). "If You Don't Understand, How Can You Learn?" GEM Policy Paper 24.
**Build Impact:**
- Validates Swahili as the primary language for Grades 1-3 content in the RAG database.
- CBC chunks must be available in both Swahili and English, with Swahili preferred for lower grades.
- Validates the voice channel (Track B3) as the highest-impact inclusion tool for pre-literate learners.

---

### [P2] UNESCO GEM Report (2023). "Technology in Education: A Tool on Whose Terms?"
**Build Impact:**
- Justifies Teacher Copilot (FR-08) as non-negotiable — not a Phase 3 nice-to-have.
- Justifies CBC alignment as a hard requirement (not generic STEM content).
- Validates pilot-first, field-validated approach before national rollout.

---

## Domain 2: Adaptive Learning — IRT & Spaced Repetition

### [P0] Lord, F.M. (1980). "Applications of Item Response Theory to Practical Testing Problems." Erlbaum.
**Summary:** Defines the 2-Parameter Logistic (2PL) IRT model:
  P(theta) = 1 / (1 + exp(-a(theta - b)))
Where theta = student ability, a = item discrimination, b = item difficulty.
**Build Impact:**
- The 2PL IRT model is the direct implementation target for src/adaptive/irt_engine.js.
- Recalibrate theta using MLE after every 10 interactions per subject-strand.
- Item parameters (a, b) pre-calibrated on 500+ CBC test questions, stored in content database.

---

### [P0] Wozniak, P.A. (1990). "Optimization of Learning." University of Poznan thesis. (SM-2 Algorithm)
**Summary:** The SuperMemo SM-2 spaced repetition algorithm. Review intervals: 1 day → 3 days → 7 days → 14 days → 30 days, with interval modifiers based on recall quality.
**Build Impact:**
- Direct implementation target for src/adaptive/spaced_repetition.js.
- Weak spots resurface after 1 day; mastered content after 30 days.
- Upgrade path: FSRS (2022) is 20-30% more efficient — implement in Phase 2.

---

### [P1] FSRS (Jarrett, 2022). "Free Spaced Repetition Scheduler."
**GitHub:** github.com/open-spaced-repetition/fsrs4anki (adopted by Anki in 2023)
**Summary:** Data-driven evolution of SM-2. Fits statistical model to user's actual review history. 20-30% fewer reviews for equivalent retention. Works with as few as 100 reviews.
**Build Impact:**
- FSRS replaces SM-2 in Phase 2 (post-pilot) for Track A.
- Track B (USSD/SMS) uses simplified SM-2 (per-user fitting not feasible via SMS).
- Implement both algorithms, feature-flag to switch.

---

## Domain 3: On-Device AI — Quantization & Tiny Language Models

### [P0] Dettmers, T., et al. (2023). "QLoRA: Efficient Finetuning of Quantized LLMs." NeurIPS 2023.
**arXiv:** 2305.14314
**Summary:** Enables fine-tuning of large LLMs on a single GPU using 4-bit NF4 quantization + LoRA adapters + Double Quantization + Paged Optimizers. A 0.4B model can be fine-tuned on Google Colab for ~$20.
**Build Impact:**
- The exact fine-tuning method for InkubaLM-0.4B on Kenya CBC curriculum.
- Recipe: InkubaLM-0.4B + QLoRA (r=16, alpha=32) + CBC Q&A pairs → CBC-InkubaLM.
- Export fine-tuned adapter as GGUF → ship via Play Asset Delivery.

---

### [P0] Hu, E., et al. (2022). "LoRA: Low-Rank Adaptation of Large Language Models." ICLR 2022.
**arXiv:** 2106.09685
**Summary:** LoRA introduces trainable rank-decomposition matrices into transformer layers, freezing original weights. Reduces trainable parameters by 10,000x with no quality loss.
**Build Impact:**
- LoRA adapters enable CBC-specific fine-tuning of InkubaLM.
- Small adapter file (~5-15MB) distributed separately from base model — allows curriculum updates without re-distributing 300MB model.
- Enables quarterly adapter updates via the sync API.

---

### [P0] Xu, Y., et al. (2024). "On-Device Language Models: A Comprehensive Review." arXiv:2409.00088.
**Summary:** GGUF + llama.cpp is dominant stack for Android. Thermal throttling reduces throughput 70% under load. Memory spikes during prefill can be 2x model file size. NNAPI acceleration inconsistent across Android OEMs.
**Build Impact:**
- Confirms GGUF + native Android inference (Kotlin + LiteRT-LM or llama.cpp JNI) is the correct stack for EduCloud's Kotlin app.
- THERMAL: Implement inference throttling after 5 consecutive questions. 30s cooldown if CPU temp > 40C.
- MEMORY: Pre-allocate context buffer at app launch. Measure peak RAM, not just model file size.
- NNAPI: Do NOT rely on NNAPI — budget Kenyan phones (Tecno/Itel) have inconsistent hardware. CPU-only inference is the baseline.

---

### [P1] Frantar, E., et al. (2023). "GPTQ: Accurate Post-Training Quantization for GPTs." ICLR 2023.
**arXiv:** 2210.17323
**Summary:** Near-lossless 4-bit quantization using one-shot Hessian-based optimization. Backbone of the GGUF Q4_K_M format.
**Build Impact:**
- Use Q4_K_M (not Q8_0 or Q5) for optimal size/quality tradeoff on 2GB devices.
- Q4_K_M: ~30% smaller than Q8, ~5% quality degradation — acceptable for curriculum Q&A.

---

### [P1] MELT Benchmark (2024). "Mobile and Edge Evaluation of Large Language Models." OpenReview.
**Summary:** Standardized benchmark for LLM performance on edge devices measuring tokens/second, energy consumption, thermal behavior, memory.
**Build Impact:**
- Use MELT as reference for our device testing matrix (Tecno Pop 7, Itel A18, Galaxy A04).
- Target: P95 < 8s on Tecno Pop 7 (2GB). MELT suggests ~250-350 tok/s on 2GB CPU-only = feasible.

---

## Domain 4: African Language NLP & Speech

### [P0] Oladipo, A., et al. (2024). "InkubaLM: A Small Language Model for Low-Resource African Languages." arXiv:2408.17024.
**Summary:** InkubaLM-0.4B trained from scratch on 2.4B tokens (1.9B in Swahili, isiZulu, Yoruba, Hausa, isiXhosa). Uses MobileLLM architecture. Competitive against much larger models on AfriMMLU and AfriXNLI.
**Build Impact:**
- Primary on-device model. Download from HuggingFace (Skier8402/InkubaLM-0.4B-Q4_K_M-GGUF), verify SHA-256, bundle via Play Asset Delivery.
- InkubaLM outperforms larger models on Swahili sentiment analysis — useful for detecting student frustration in voice interactions.
- Inkuba-Instruct dataset is the starting point for CBC fine-tuning dataset.

---

### [P0] Adelani, D., et al. (2022). "A Few Thousand Translations Go a Long Way!" NAACL 2022.
**Summary:** Small amounts of parallel translation data (~few thousand sentences) dramatically improve multilingual model performance when combined with pre-trained multilingual models.
**Build Impact:**
- CBC content pipeline: translate ~2,000 key curriculum passages EN→SW using Helsinki-NLP Opus-MT (free) → manually verify 10% → use both in RAG database.
- Hybrid Swahili/English database ensures RAG retrieval works for questions in either language.

---

### [P0] Radford, A., et al. (2023). "Robust Speech Recognition via Large-Scale Weak Supervision." ICML 2023. (Whisper)
**arXiv:** 2212.04356
**Summary:** Whisper trained on 680,000 hours of web audio in 99 languages including Swahili. State-of-the-art STT. Medium is the best tradeoff for server deployment.
**Build Impact:**
- Use whisper-medium for cloud STT server — 1.5GB model, faster inference.
- Fine-tune on AfriSpeech-200 + WAXAL for Kenyan Swahili accents.
- Telephony audio (G.711 8kHz) must be resampled to 16kHz before Whisper.
- Implement VAD before STT to skip silence — reduces processing time 30-50%.

---

### [P1] WAXAL Consortium (2026). "WAXAL: Open-Access Speech Dataset for 21 Sub-Saharan African Languages." arXiv preprint.
**Summary:** 11,000+ hours of speech across Swahili, Hausa, Yoruba, Igbo, Luganda, and 16 other languages. High-quality recordings for TTS + natural speech for STT.
**Build Impact:**
- Fine-tune Whisper on WAXAL Swahili split (~500h) for Kenya-specific accent robustness.
- Use WAXAL TTS data to fine-tune Piper/Kokoro for natural Swahili TTS.
- Key differentiator: competitors without WAXAL fine-tuning will have worse Swahili STT accuracy.

---

### [P1] IntronHealth (2024). "AfriSpeech-200: Pan-African Accented Speech Dataset." ACL Anthology / HuggingFace.
**Summary:** 200+ hours of African-accented speech across 15+ countries. Model intronhealth/afrispeech-whisper-medium-all already fine-tuned and available.
**Build Impact:**
- START with intronhealth/afrispeech-whisper-medium-all as STT baseline for Track B3 — saves months of fine-tuning.
- Add WAXAL Swahili data in Phase 2 for Kenya-specific improvement.
- Test separately for Nairobi urban vs. rural accents — expect 10-15% WER difference.

---

## Domain 5: Retrieval-Augmented Generation (RAG)

### [P0] Lewis, P., et al. (2020). "Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks." NeurIPS 2020.
**arXiv:** 2005.11401
**Summary:** Foundational RAG paper. Combines retrieval (dense vectors) with generative LLM. Retrieval constrains the LLM to answers grounded in retrieved documents, dramatically reducing hallucination.
**Build Impact:**
- Architecture justification for the entire RAG pipeline (FR-01).
- Key insight: retrieval quality matters more than LLM size. Small LLM + excellent retrieval > large LLM without it.
- Our pipeline: multilingual-e5-small → sqlite-vec → InkubaLM-0.4B exactly matches this architecture.

---

### [P0] RAGdb (arXiv, 2025). "A Zero-Dependency Embeddable Architecture for Multimodal RAG on the Edge."
**Summary:** Proposes "Single-File Knowledge Container": all documents, embeddings, and retrieval logic in one SQLite file. Zero cloud dependencies. ONNX for embedding inference.
**Build Impact:**
- Validates sqlite-vec as the correct vector store for Track A and Track C.
- Implement hybrid retrieval: FTS5 (full-text search) + vector cosine similarity → Reciprocal Rank Fusion (RRF).
- Single-file concept means we distribute the entire RAG knowledge base as one .db file via SD card or Play Asset Delivery.

---

### [P1] "Retrieval-Augmented Generation in Multilingual Settings." ACL Anthology (2024/2025).
**Summary:** RAG across 13+ languages. Cross-lingual RAG works well with multilingual embedding models. Monolingual retrieval (question and docs in same language) is 5-10% more accurate.
**Build Impact:**
- Use multilingual-e5-small (not English-only all-MiniLM-L6-v2).
- Store CBC chunks in BOTH Swahili and English. Retrieve in the student's language.
- Embed questions in their original language — do not translate before embedding.

---

### [P1] "Curriculum-Aware RAG for Educational Systems." ResearchGate (2024).
**Summary:** RAG systems for curriculum materials achieve higher F1 when chunks match the curriculum hierarchy (Grade > Subject > Strand > Topic > Concept).
**Build Impact:**
- Structure RAG chunks with metadata: {grade, subject, strand, topic, difficulty}.
- Filter retrieval by grade and subject BEFORE vector search.
- sqlite-vec query: WHERE grade = ? AND subject = ? ORDER BY vec_distance(embedding, ?) LIMIT 3

---

## Domain 6: Voice AI & Telephony

### [P0] Carnegie Mellon / Yiya Solutions (2023). "Evaluating USSD-Based STEM Learning in Rural Uganda."
**Source:** yiyasolutions.org/research + CMU-Africa collaboration
**Summary:** Yiya's AirVoice serves 53,000+ learners in Uganda via USSD + AI phone call. Voice is 3x more engaging than USSD text for low-literacy learners. DTMF menus reduce latency. Cost: ~$0.05 per student per session.
**Build Impact:**
- Direct proof-of-concept for our Track B3 voice channel.
- DTMF-first menu design (press 1 for Math, press 2 for Science) BEFORE free-form voice.
- Session completion rate target: >70% (Yiya achieves ~75% in best schools).
- Partner opportunity: contact Yiya for data-sharing and architecture knowledge transfer.

---

### [P1] Voice AI Pipeline Latency Research (2023-2024). Multiple papers.
**Summary:** Voice AI pipelines (STT → LLM → TTS over telephone) achieve P50 < 3s, P95 < 8s with streaming optimization. Key: stream TTS as LLM generates, use smaller STT for real-time, cache common responses.
**Build Impact:**
- Stream TTS output before LLM response is complete (token-by-token TTS streaming).
- Pre-cache greetings, menu prompts, and top 50 CBC question responses — serves in <500ms.
- Latency budget: Whisper STT (~1s) + Gemini Flash (~0.5-1s) + Kokoro TTS (~0.5s) = ~2-2.5s P50.

---

## Domain 7: Federated Learning

### [P1] McMahan, H.B., et al. (2017). "Communication-Efficient Learning of Deep Networks from Decentralized Data." AISTATS 2017. (FedAvg)
**arXiv:** 1602.05629
**Summary:** FedAvg algorithm: models trained locally; only gradient updates (not raw data) sent to server. Reduces communication 10-100x.
**Build Impact:**
- FedAvg is the target algorithm for Phase 3 Federated Intelligence (DIFFERENTIATION_BRAINSTORM.md IDEA 9).
- Implementation: Flower (flwr) framework on Django server; TF Lite for on-device gradient computation.
- Privacy guarantee: no raw student data ever leaves the device.
- Narrative: "Our AI is trained by Kenyan students, for Kenyan students."

---

### [P1] Kairouz, P., et al. (2021). "Advances and Open Problems in Federated Learning." Foundations and Trends in ML.
**arXiv:** 1912.04977
**Summary:** Comprehensive FL challenges: non-IID data, partial participation, communication efficiency, privacy guarantees.
**Build Impact:**
- Non-IID: Students in Turkana use different vocabulary than Nairobi. Use FedProx, not vanilla FedAvg.
- Partial participation: only 10-20% of devices sync per week. Design for sparse participation.
- Read before Phase 2 to design sync API in a FL-compatible way.

---

## Domain 8: Privacy, Security & Data Protection

### [P0] Kenya Data Protection Act 2019 (DPA). Kenya Gazette Supplement No. 170.
**Summary:** Kenya's GDPR-equivalent for processing children's data. Requires: parental consent, data minimization, purpose limitation, right to erasure, encryption at rest, DPIA before launch.
**Build Impact:**
- No student data collection without verified parental consent.
- SQLite encrypted with SQLCipher (AES-256-CBC) on Track A devices.
- Student names stored as pseudonyms by default.
- DPIA document required before pilot launch — mandatory gate in PRD Section 7.
- Data retention: auto-purge after 2 years of inactivity.

---

### [P1] OWASP Mobile Security Testing Guide (MSTG) 2024.
**Source:** owasp.org/OWASP-Mobile-Security-Testing-Guide
**Build Impact:**
- Android Keystore for device_token storage — no plaintext credentials in SharedPreferences.
- Certificate pinning for sync API endpoint — prevents MITM on school networks.
- OWASP Mobile Top 10 audit before pilot launch.
- ProGuard/R8 code obfuscation in production build.

---

### [P2] W3C Verifiable Credentials Data Model v2.0 (2024). W3C Recommendation.
**Source:** w3.org/TR/vc-data-model
**Summary:** Standard for cryptographically verifiable digital credentials using Ed25519 signatures. No blockchain required.
**Build Impact:**
- Use W3C VC for all Education Cloud certificates (FR-12).
- Sign with Ed25519 server key. Verify via GET /api/v1/certificates/verify/{id}.
- No blockchain needed — PKI with server key is sufficient for MVP.

---

## Domain 9: Behavioral Science & Engagement

### [P0] Ebbinghaus, H. (1885). "Uber das Gedachtnis." Leipzig: Duncker & Humblot.
**Summary:** The forgetting curve: humans forget ~50% of new information within 1 hour, ~70% within 24 hours, ~90% within 1 week. Cure: spaced repetition reviews at increasing intervals before the forgetting threshold.
**Build Impact:**
- Scientific justification for the SM-2/FSRS streak system (FR-11).
- 7:00 AM daily SMS trigger fires BEFORE the forgetting curve drops below 50%.
- SM-2 intervals (1→3→7→14→30 days) calibrated to land just before forgetting threshold.
- Parent Loop weekly reports (Friday 6 PM) align with the 7-day forgetting interval.

---

### [P1] Duolingo Research (2022). "How Streaks Changed Everything: The Psychology of Habit Formation."
**Source:** research.duolingo.com
**Summary:** Streak preservation drives 40%+ of DAU retention. Loss aversion (fear of losing a streak) is stronger than gain framing (earning points). Streak freeze reduces churn 15%.
**Build Impact:**
- Implement streak freeze (1 per week) exactly as in FR-11 — validated behavioral science.
- SMS strategy: use LOSS framing ("You'll lose your 14-day streak!") not gain framing ("Earn 10 stars!").
- Milestone moments at 7, 14, 30, 90, 365 days — each generates shareable SMS to parents.
- Display streak prominently in UI (top of screen, visible before any lesson).

---

### [P2] Deci, E.L. & Ryan, R.M. (1985). "Intrinsic Motivation and Self-Determination." Springer. (SDT)
**Summary:** Self-Determination Theory: humans are intrinsically motivated when needs for Autonomy, Competence, and Relatedness are met.
**Build Impact:**
- Autonomy: Let students choose subject and topic. Don't force linear paths.
- Competence: IRT difficulty adapts so students are always challenged but not overwhelmed (target 60-70% correct).
- Relatedness: Parent Loop, school leaderboards, and Classroom Mesh address the relatedness need.

---

## Domain 10: Infrastructure & Offline-First

### [P1] Kleppmann, M. (2017). "Designing Data-Intensive Applications." O'Reilly.
**Key chapters:** Replication, Transactions, Batch Processing
**Build Impact:**
- Sync API uses Last-Write-Wins (LWW) with device-local timestamps — consistent with Kleppmann's recommendations for append-only educational data.
- Conflict resolution: take higher score per topic when merging (optimistic merge).
- Retry with exponential backoff (1s, 2s, 4s, 8s, max 60s) for sync failures.

---

### [P2] Raspberry Pi Foundation (2024). "Raspberry Pi 5 for Educational Deployment."
**Summary:** Pi 5 4GB: ~2 tok/s for Qwen2-0.5B Q4_K_M via llama.cpp CPU-only. Pi 5 8GB: ~3-4 tok/s. At 2 tok/s, a 100-token response takes ~50 seconds — too slow for interactive use.
**Build Impact:**
- CRITICAL: Upgrade School Hub spec from Pi 5 4GB to Pi 5 8GB ($80 vs $60) for acceptable latency.
- Implement request queuing (max 3 concurrent) with progress spinner.
- Pre-compute responses for top 50 CBC questions → serve from cache (<500ms).
- Alternative: Pi 5 4GB with AI HAT+ NPU ($70 add-on) for 3-5x faster inference.

---

## Master Bibliography (31 Core References)

### Foundational Pedagogy
1. Bloom, B.S. (1984). "The 2 Sigma Problem." Educational Researcher, 13(6), 4-16.
2. VanLehn, K. (2011). "The Relative Effectiveness of Human Tutoring..." Educational Psychologist, 46(4), 197-221.
3. Kulik, J.A. & Fletcher, J.D. (2016). "Effectiveness of Intelligent Tutoring Systems." Review of Educational Research, 86(1), 42-78.
4. UNESCO. (2016). "If You Don't Understand, How Can You Learn?" GEM Policy Paper 24.
5. UNESCO. (2023). "Technology in Education: A Tool on Whose Terms?" GEM Report 2023.
6. Ebbinghaus, H. (1885). "Uber das Gedachtnis." Leipzig: Duncker & Humblot.
7. Deci, E.L. & Ryan, R.M. (1985). Intrinsic Motivation and Self-Determination. Springer.

### Adaptive Learning
8. Lord, F.M. (1980). Applications of Item Response Theory. Erlbaum.
9. Wozniak, P.A. (1990). "Optimization of Learning." University of Poznan thesis. (SM-2)
10. Jarrett, L. (2022). "Free Spaced Repetition Scheduler (FSRS)." github.com/open-spaced-repetition/fsrs4anki.

### On-Device AI
11. Dettmers, T., et al. (2023). "QLoRA: Efficient Finetuning of Quantized LLMs." NeurIPS 2023. arXiv:2305.14314.
12. Hu, E., et al. (2022). "LoRA: Low-Rank Adaptation of Large Language Models." ICLR 2022. arXiv:2106.09685.
13. Frantar, E., et al. (2023). "GPTQ: Accurate Post-Training Quantization for GPTs." ICLR 2023. arXiv:2210.17323.
14. Xu, Y., et al. (2024). "On-Device Language Models: A Comprehensive Review." arXiv:2409.00088.
15. MELT Benchmark (2024). "Mobile and Edge Evaluation of Large Language Models." OpenReview.

### African Language NLP & Speech
16. Oladipo, A., et al. (2024). "InkubaLM: A Small Language Model for Low-Resource African Languages." arXiv:2408.17024.
17. Adelani, D., et al. (2022). "A Few Thousand Translations Go a Long Way!" NAACL 2022.
18. Radford, A., et al. (2023). "Robust Speech Recognition via Large-Scale Weak Supervision." (Whisper) ICML 2023. arXiv:2212.04356.
19. WAXAL Consortium. (2026). "WAXAL: Open-Access Speech Dataset for 21 Sub-Saharan African Languages." arXiv preprint.
20. IntronHealth. (2024). "AfriSpeech-200: Pan-African Accented Speech Dataset." ACL Anthology / HuggingFace.

### Retrieval-Augmented Generation
21. Lewis, P., et al. (2020). "Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks." NeurIPS 2020. arXiv:2005.11401.
22. RAGdb. (2025). "Zero-Dependency Embeddable Architecture for Multimodal RAG on the Edge." arXiv.
23. "Retrieval-Augmented Generation in Multilingual Settings." ACL Anthology (2024/2025).

### Voice AI & Mobile Telephony Education
24. Carnegie Mellon / Yiya Solutions. (2023). "Evaluating USSD-Based STEM Learning in Rural Uganda." CMU-Africa.
25. Africa's Talking. (2024). "Voice API Developer Documentation." developers.africastalking.com.

### Federated Learning
26. McMahan, H.B., et al. (2017). "Communication-Efficient Learning of Deep Networks from Decentralized Data." AISTATS 2017. arXiv:1602.05629.
27. Kairouz, P., et al. (2021). "Advances and Open Problems in Federated Learning." Foundations and Trends in ML. arXiv:1912.04977.

### Privacy & Compliance
28. Kenya Data Protection Act 2019. Kenya Gazette Supplement No. 170.
29. OWASP. (2024). "Mobile Security Testing Guide." owasp.org.
30. W3C. (2024). "Verifiable Credentials Data Model v2.0." w3.org/TR/vc-data-model.

### Behavioral Science
31. Duolingo Research. (2022). "How Streaks Changed Everything." research.duolingo.com.

---

## Quick Reference: Papers to PRD Features

| PRD Feature | Key Papers | Immediate Build Action |
|---|---|---|
| FR-01: On-Device AI | #11, #12, #14, #16 | QLoRA fine-tune InkubaLM-0.4B on CBC Q&A |
| FR-02: USSD | #24 | Copy Yiya's DTMF-first menu design |
| FR-04: Voice Call | #18, #19, #20, #24 | Start with intronhealth/afrispeech-whisper-medium-all |
| FR-06: Adaptive IRT | #8, #9, #10 | Implement 2PL IRT in irt_engine.js |
| FR-09: School Hub | Pi Foundation docs | Upgrade to Pi 5 8GB |
| FR-11: Streaks | #6, #31 | Use loss-framing in SMS copy |
| FR-12: Certificates | #30 | Use W3C VC Data Model v2.0 |
| Pedagogical Design | #1, #2, #3 | Implement Socratic system prompt |
| RAG Pipeline | #21, #22, #23 | multilingual-e5-small + hybrid FTS5+vec |
| Privacy/Legal | #28, #29 | DPIA before pilot; SQLCipher on device |

---

*Compiled: July 2026 | Education Cloud Research Team*
*Sources: arXiv, ACL Anthology, NeurIPS, ICML, UNESCO, CMU-Africa, HuggingFace, W3C, OWASP*
