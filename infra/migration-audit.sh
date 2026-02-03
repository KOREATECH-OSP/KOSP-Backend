#!/bin/bash
# MSA Migration Audit Script
# Verifies that the MSA migration is complete by checking for legacy code removal
# and verifying new architecture components are in place

set -e

echo "========================================="
echo "KOSP MSA Migration Audit"
echo "========================================="
echo ""

FAILED=0

# Check 1: No Redis Stream references
echo "[CHECK 1] No Redis Stream references in backend..."
if grep -r "StreamReceiver\|RedisMessageListenerContainer" --include="*.java" backend/src 2>/dev/null; then
    echo "❌ FAILED: Redis Stream references found"
    FAILED=1
else
    echo "✅ PASSED: No Redis Stream references"
fi
echo ""

# Check 2: No old stream keys
echo "[CHECK 2] No old stream keys in backend..."
if grep -r "kosp:challenge-check\|github:collection:trigger" --include="*.java" backend/src 2>/dev/null; then
    echo "❌ FAILED: Old stream keys found"
    FAILED=1
else
    echo "✅ PASSED: No old stream keys"
fi
echo ""

# Check 3: JobQueueService still used
echo "[CHECK 3] JobQueueService still used..."
if grep -r "JobQueueService" --include="*.java" backend/src 2>/dev/null >/dev/null; then
    echo "✅ PASSED: JobQueueService found (Backend → Harvester)"
else
    echo "❌ FAILED: JobQueueService not found"
    FAILED=1
fi
echo ""

# Check 4: RabbitMQ imports in services
echo "[CHECK 4] RabbitMQ imports in services..."
SERVICES=("api-service" "challenge-service" "notification-service")
for SERVICE in "${SERVICES[@]}"; do
    if [ -d "$SERVICE/src" ]; then
        if grep -r "org.springframework.amqp" --include="*.java" "$SERVICE/src" 2>/dev/null >/dev/null; then
            echo "✅ PASSED: $SERVICE has RabbitMQ imports"
        else
            echo "❌ FAILED: $SERVICE missing RabbitMQ imports"
            FAILED=1
        fi
    else
        echo "⚠️  SKIPPED: $SERVICE directory not found"
    fi
done
echo ""

# Check 5: OutboxPublisher active
echo "[CHECK 5] OutboxPublisher active..."
if grep -r "OutboxPublisher" --include="*.java" infra 2>/dev/null >/dev/null; then
    echo "✅ PASSED: OutboxPublisher found"
else
    echo "❌ FAILED: OutboxPublisher not found"
    FAILED=1
fi
echo ""

echo "========================================="
if [ $FAILED -eq 0 ]; then
    echo "✅ ALL CHECKS PASSED"
    echo "========================================="
    exit 0
else
    echo "❌ SOME CHECKS FAILED"
    echo "========================================="
    exit 1
fi
