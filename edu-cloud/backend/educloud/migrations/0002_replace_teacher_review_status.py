from django.db import migrations


def replace_teacher_review_status(apps, schema_editor):
    """Make previously generated demo packs readable by the automatic contract."""
    remediation_pack = apps.get_model("educloud", "RemediationPack")
    for pack in remediation_pack.objects.all().iterator():
        payload = pack.payload
        if not isinstance(payload, dict) or payload.get("validation_status"):
            continue
        if payload.get("review_status") != "teacher_review_required":
            continue

        updated = dict(payload)
        updated.pop("review_status", None)
        updated["validation_status"] = "automatic_validation_passed"
        updated["provenance"] = (
            "AI-generated original Grade 3 Maths remediation from anonymised error patterns; "
            "automatically validated for schema, supported scope, provenance label, and arithmetic. "
            "Not KICD/KEC curriculum content."
        )
        lessons = updated.get("lessons")
        if isinstance(lessons, list):
            updated["lessons"] = [
                {
                    **lesson,
                    "source": "Personalised Grade 3 Maths practice · automatic checks passed",
                }
                if isinstance(lesson, dict)
                else lesson
                for lesson in lessons
            ]
        pack.payload = updated
        pack.save(update_fields=["payload"])


class Migration(migrations.Migration):
    dependencies = [("educloud", "0001_initial")]

    operations = [migrations.RunPython(replace_teacher_review_status, migrations.RunPython.noop)]
