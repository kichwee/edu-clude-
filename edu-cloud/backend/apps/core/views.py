"""
Core app: middleware, health checks, Prometheus metrics, exceptions, i18n.
"""
import uuid
import time
import structlog
import structlog.contextvars
from django.http import JsonResponse
from django.db import connection
from django_redis import get_redis_connection

logger = structlog.get_logger('educloud.core')


def health_check(request):
    """GET /api/v1/health — basic liveness (FR-15)"""
    return JsonResponse({'status': 'ok', 'service': 'education-cloud'})


def readiness_check(request):
    """
    GET /api/v1/ready — deep readiness check (FR-15)
    Returns 503 if any dependency is unhealthy.
    """
    checks = {}
    errors = []

    # Check PostgreSQL
    try:
        connection.ensure_connection()
        checks['postgres'] = 'ok'
    except Exception as e:
        checks['postgres'] = 'error'
        errors.append(f'postgres: {e}')

    # Check Redis
    try:
        redis = get_redis_connection('default')
        redis.ping()
        checks['redis'] = 'ok'
    except Exception as e:
        checks['redis'] = 'error'
        errors.append(f'redis: {e}')

    # Check Africa's Talking (lightweight — just verify key is set)
    from django.conf import settings
    if settings.AFRICASTALKING_API_KEY:
        checks['africastalking'] = 'configured'
    else:
        checks['africastalking'] = 'missing_key'
        errors.append('africastalking: API key not set')

    if errors:
        return JsonResponse(
            {'status': 'unhealthy', 'checks': checks, 'errors': errors},
            status=503,
        )
    return JsonResponse({'status': 'healthy', 'checks': checks})
