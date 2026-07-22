"""
Sync API — Track A (On-Device) to Cloud Synchronization
Implements FR-01 Sync Contract. Receives analytics and offline interactions from the React Native app.
Uses Last-Write-Wins (LWW) conflict resolution for offline records.
"""
import json
import structlog
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST
from django.http import JsonResponse

from apps.students.models import Student, Interaction, IRTParameter

logger = structlog.get_logger('educloud.sync')

@csrf_exempt
@require_POST
def sync_data(request):
    """
    POST /api/v1/sync
    Accepts JSON payload from Track A app, updates student state.
    """
    # Note: In production, enforce authentication via DeviceTokenAuthentication
    try:
        data = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)
        
    device_id = data.get('device_id')
    if not device_id:
        return JsonResponse({'error': 'device_id required'}, status=400)
        
    # Get or create student mapping for this device
    student, _ = Student.objects.get_or_create(
        device_id=device_id,
        track='A',
        defaults={
            'alias': f"User-{device_id[:6]}",
            'grade': 5,
        }
    )
    
    log = logger.bind(student_id=student.id, device=device_id)
    
    # Process IRT Scores
    scores = data.get('scores', [])
    for score in scores:
        subj = score.get('subject')
        strand = score.get('strand', 'general')
        new_theta = score.get('theta')  # Allow device to compute its own IRT theta when offline
        
        if subj and new_theta is not None:
            param, _ = IRTParameter.objects.get_or_create(student=student, subject=subj, strand=strand)
            # Last-Write-Wins: if device synced, update cloud
            param.ability_theta = new_theta
            param.save(update_fields=['ability_theta'])
            
    # Process Analytics
    analytics = data.get('analytics', {})
    if analytics:
        log.info('sync_analytics', **analytics)
        
    # Respond with content updates (stubbed for MVP)
    return JsonResponse({
        'status': 'ok',
        'new_content_available': False,
        'content_url': None,
        'content_size_bytes': 0
    })
