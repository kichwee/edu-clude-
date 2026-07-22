"""
Item Response Theory (IRT) Engine for Adaptive Learning
Implements a 2-Parameter Logistic (2PL) model with Maximum Likelihood Estimation
(MLE) calibration after every 10 interactions (Newton-Raphson).

PRD FR-06 & Research Foundation: 2-Parameter IRT with MLE calibration.
"""
import math
# pyrefly: ignore [missing-import]
import structlog#
from django.utils import timezone
from apps.students.models import Student, IRTParameter, Interaction

logger = structlog.get_logger('educloud.irt')

# ─── IRT Constants ────────────────────────────────────────────────────────────
# Theta bounds to prevent runaway scores
MIN_THETA = -3.0
MAX_THETA = 3.0
CALIBRATION_INTERVAL = 10  # Interactions before recalibrating
LEARNING_RATE = 0.4        # K-factor equivalent for fast Elo-like updates


class IRTEngine:
    @staticmethod
    def get_theta(student: Student, subject: str, strand: str) -> float:
        """Get current student ability (theta) for a subject/strand."""
        param, _ = IRTParameter.objects.get_or_create(
            student=student,
            subject=subject,
            strand=strand,
            defaults={'ability_theta': 0.0}
        )
        return param.ability_theta

    @staticmethod
    def probability_correct(theta: float, difficulty_b: float, discrimination_a: float = 1.0) -> float:
        """
        2PL (Two-Parameter Logistic) model probability.
        P(X=1 | theta, b, a) = 1 / (1 + e^-(a * (theta - b)))
        """
        # Clamp exponent to prevent overflow
        x = max(min(discrimination_a * (theta - difficulty_b), 10), -10)
        return 1.0 / (1.0 + math.exp(-x))

    @staticmethod
    def record_response(student: Student, subject: str, strand: str, is_correct: bool, difficulty_b: float = 0.0, discrimination_a: float = 1.0):
        """
        Update student ability based on response.
        Uses gradient ascent for fast real-time updates, and MLE calibration every 10 interactions.
        """
        param, _ = IRTParameter.objects.get_or_create(
            student=student,
            subject=subject,
            strand=strand,
            defaults={'ability_theta': 0.0}
        )
        
        expected_prob = IRTEngine.probability_correct(param.ability_theta, difficulty_b, discrimination_a)
        actual_score = 1.0 if is_correct else 0.0
        
        # Calculate real-time Elo-like update
        update = LEARNING_RATE * (actual_score - expected_prob)
        new_theta = max(MIN_THETA, min(MAX_THETA, param.ability_theta + update))
        
        param.ability_theta = new_theta
        param.items_seen += 1
        
        if param.items_seen % CALIBRATION_INTERVAL == 0:
            new_theta = IRTEngine.calibrate_mle_newton_raphson(student, subject, strand, param)
            
        param.save(update_fields=['ability_theta', 'items_seen', 'last_calibrated'])
        return new_theta

    @staticmethod
    def calibrate_mle_newton_raphson(student: Student, subject: str, strand: str, param: IRTParameter) -> float:
        """
        Perform Maximum Likelihood Estimation using Newton-Raphson method
        on the last N interactions to find the optimal theta.
        """
        interactions = Interaction.objects.filter(
            student=student, subject=subject, strand=strand
        ).order_by('-created_at')[:CALIBRATION_INTERVAL]

        if not interactions:
            return param.ability_theta

        theta = param.ability_theta
        
        # Newton-Raphson for up to 5 iterations
        for _ in range(5):
            d1 = 0.0  # First derivative (gradient)
            d2 = 0.0  # Second derivative (Hessian)
            
            for inter in interactions:
                a = getattr(inter, 'discrimination_a', 1.0)
                b = inter.difficulty
                u = 1.0 if inter.is_correct else 0.0
                
                p = IRTEngine.probability_correct(theta, b, a)
                
                d1 += a * (u - p)
                d2 -= (a ** 2) * p * (1.0 - p)
            
            # Avoid division by zero
            if abs(d2) < 1e-5:
                break
                
            step = d1 / d2
            theta = theta - step
            
            # Clamp theta to bounds
            theta = max(MIN_THETA, min(MAX_THETA, theta))
            
            # Converged if step is very small
            if abs(step) < 1e-3:
                break
                
        param.ability_theta = theta
        param.last_calibrated = timezone.now()
        logger.info('irt_calibrated_mle', student=student.id, subject=subject, new_theta=theta)
        return theta

    @staticmethod
    def sm2_next_interval(
        repetitions: int,
        easiness_factor: float,
        quality: int,
        previous_interval: int = 1,
    ) -> tuple[int, float]:
        """
        SuperMemo-2 (SM-2) Spaced Repetition Algorithm (FR-06)
        Quality: 0-5 (0=blank, 3=correct but hard, 5=perfect)
        Returns (next_interval_days, new_easiness_factor)
        """
        if quality < 3:
            repetitions = 0
            interval = 1
        else:
            if repetitions == 0:
                interval = 1
            elif repetitions == 1:
                interval = 6
            else:
                interval = round(previous_interval * easiness_factor)
            repetitions += 1

        ef = easiness_factor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        ef = max(1.3, ef)

        return interval, ef
