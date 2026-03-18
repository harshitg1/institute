package com.institute.Institue.tenant;

public class TenantContext {
    private static final ThreadLocal<String> currentOrg = new ThreadLocal<>();

    public static void setCurrentOrg(String orgId) {
        currentOrg.set(orgId);
    }

    public static String getCurrentOrgId() {
        return currentOrg.get();
    }

    public static void clear() {
        currentOrg.remove();
    }
}

