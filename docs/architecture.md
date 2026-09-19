# Pocket Vision — System Architecture Document

## Design Principles
1. **Responsiveness**: Camera preview runs at native 30–60 FPS. Image analysis runs decoupled asynchronously using CameraX `STRATEGY_KEEP_ONLY_LATEST`.
2. **Thermal & Battery Awareness**: Frame analysis does not process every single camera frame; inference runs at 10–12 FPS with time-multiplexed model execution.
3. **Privacy & Security**: All live object, gesture, and face perception is computed 100% on-device. No images are streamed to the cloud continuously. Smart Scan only transmits intentionally selected single frames.
4. **Ethical Vision Boundaries**: Visible physical facial expressions (smiling, neutral, frowning, surprised) are classified without speculative psychological inferences.
