#!/bin/bash

BASE_URL="http://localhost:8084/api/tour-guides"

echo "=== 1. Register a Tour Guide ==="
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Verification",
    "lastName": "Test",
    "email": "verify.test@example.com",
    "phoneNumber": "+94770000000",
    "bio": "Test bio",
    "languages": ["English"],
    "specializations": ["wildlife"],
    "experienceYears": 5,
    "hourlyRate": 20.00,
    "dailyRate": 120.00
  }')

echo "$REGISTER_RESPONSE" | jq .
GUIDE_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.data.id')

if [ "$GUIDE_ID" == "null" ] || [ -z "$GUIDE_ID" ]; then
  echo "Failed to register guide."
  exit 1
fi

echo "Created Guide ID: $GUIDE_ID"
echo ""

echo "=== 2. Search Tour Guides ==="
curl -s -X GET "$BASE_URL" | jq .
echo ""

echo "=== 3. Get Tour Guide by ID ($GUIDE_ID) ==="
curl -s -X GET "$BASE_URL/$GUIDE_ID" | jq .
echo ""

echo "=== 4. Update Tour Guide (by ID: $GUIDE_ID) ==="
curl -s -X PUT "$BASE_URL/$GUIDE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Verification",
    "lastName": "Updated",
    "email": "verify.test@example.com",
    "phoneNumber": "+94770000000",
    "bio": "Updated test bio",
    "languages": ["English", "Sinhala"],
    "specializations": ["wildlife", "adventure"],
    "experienceYears": 6,
    "hourlyRate": 25.00,
    "dailyRate": 150.00
  }' | jq .
echo ""

echo "=== 5. Check Availability (ID: $GUIDE_ID) ==="
curl -s -X GET "$BASE_URL/$GUIDE_ID/availability?startDate=2026-04-01&endDate=2026-04-05" | jq .
echo ""

echo "=== 6. Get My Profile (Mocked) ==="
curl -s -X GET "$BASE_URL/me" | jq .
echo ""

echo "=== 7. Update My Profile (Mocked) ==="
curl -s -X PUT "$BASE_URL/me" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Me",
    "lastName": "Profile",
    "email": "me.profile@example.com",
    "phoneNumber": "+94771111111",
    "bio": "My profile bio",
    "languages": ["English"],
    "specializations": ["cultural"],
    "experienceYears": 2,
    "hourlyRate": 15.00,
    "dailyRate": 100.00
  }' | jq .
echo ""

echo "=== 8. Soft Delete Tour Guide (ID: $GUIDE_ID) ==="
curl -s -i -X DELETE "$BASE_URL/$GUIDE_ID"
echo ""

echo "=== 9. Verify Deactivation (ID: $GUIDE_ID) ==="
curl -s -X GET "$BASE_URL/$GUIDE_ID" | jq .
echo ""
