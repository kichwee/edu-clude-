import hmac
import hashlib
from functools import wraps
from django.conf import settings
from django.http import HttpResponseForbidden

def validate_at_signature(view_func):
    """
    Decorator to validate Africa's Talking webhook requests.
    Checks either the apiKey header or the X-Signature HMAC header.
    """
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        api_key = request.headers.get('apiKey') or request.headers.get('apikey')
        signature = request.headers.get('X-Signature')
        
        # 1. Simple API Key header check
        if api_key and api_key == getattr(settings, 'AFRICASTALKING_API_KEY', ''):
            return view_func(request, *args, **kwargs)
            
        # 2. HMAC Signature verification (if X-Signature is used)
        if signature:
            payload = request.body
            expected_sig = hmac.new(
                getattr(settings, 'AFRICASTALKING_API_KEY', '').encode('utf-8'),
                payload,
                hashlib.sha256
            ).hexdigest()
            if hmac.compare_digest(signature, expected_sig):
                return view_func(request, *args, **kwargs)
        
        # 3. Deny if in production and no valid auth
        if not getattr(settings, 'DEBUG', False):
            return HttpResponseForbidden("Invalid Africa's Talking signature")
            
        return view_func(request, *args, **kwargs)

    return _wrapped_view
