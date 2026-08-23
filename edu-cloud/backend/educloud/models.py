"""Persistence for anonymised, automatically validated remediation packs."""

from django.db import models


class RemediationPack(models.Model):
    """One generated offline patch, keyed only by a random device UUID.

    The original attempt records are not persisted.  ``telemetry_digest`` is
    retained only to make a replay traceable during the hackathon demo.
    """

    learner_id = models.UUIDField(db_index=True)
    request_id = models.UUIDField(unique=True)
    content_version = models.CharField(max_length=96)
    telemetry_digest = models.CharField(max_length=64)
    payload = models.JSONField()
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at"]
