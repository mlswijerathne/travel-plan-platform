-- ============================================================
-- RBAC: Secure role promotion on user signup
-- ============================================================
-- Security model:
--   • user_metadata.role  → set by the user during signup (writable by client)
--   • app_metadata.role   → set by this trigger (writable only by service role)
--
-- Only app_metadata is trusted by the backend JWT filter.
-- ADMIN role cannot be self-assigned — it is set exclusively via the Admin API.
-- ============================================================

CREATE OR REPLACE FUNCTION public.handle_new_user_role()
RETURNS TRIGGER AS $$
DECLARE
  requested_role TEXT;
  -- Roles that a user may self-assign at registration.
  -- ADMIN is intentionally excluded — must be set via Admin API.
  allowed_self_assign_roles TEXT[] := ARRAY[
    'TOURIST',
    'HOTEL_OWNER',
    'TOUR_GUIDE',
    'VEHICLE_OWNER'
  ];
BEGIN
  requested_role := NEW.raw_user_meta_data->>'role';

  -- Validate: reject unknown or missing roles; default to TOURIST
  IF requested_role IS NULL OR NOT (requested_role = ANY(allowed_self_assign_roles)) THEN
    requested_role := 'TOURIST';
  END IF;

  -- Promote to app_metadata (admin-controlled, cannot be modified by client SDK)
  UPDATE auth.users
  SET raw_app_meta_data = raw_app_meta_data || jsonb_build_object('role', requested_role)
  WHERE id = NEW.id;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Drop and recreate trigger to ensure idempotency
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_user_role();
