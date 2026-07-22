# Generated manually to keep the hackathon demo schema small and reviewable.

from django.db import migrations, models


class Migration(migrations.Migration):
    initial = True

    dependencies = []

    operations = [
        migrations.CreateModel(
            name="RemediationPack",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("learner_id", models.UUIDField(db_index=True)),
                ("request_id", models.UUIDField(unique=True)),
                ("content_version", models.CharField(max_length=96)),
                ("telemetry_digest", models.CharField(max_length=64)),
                ("payload", models.JSONField()),
                ("created_at", models.DateTimeField(auto_now_add=True)),
            ],
            options={"ordering": ["-created_at"]},
        ),
    ]
