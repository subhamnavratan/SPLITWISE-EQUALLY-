// api.js

const API_BASE_URL = "http://localhost:8080";

export const API = {
    // --- USER ENDPOINTS ---
    register: (data) => fetch(`${API_BASE_URL}/api/users/register`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) }),
    login: (identifier) => fetch(`${API_BASE_URL}/api/users/login/${identifier}`),

    // --- GROUP ENDPOINTS (Uses Phone/GroupName) ---
    getGroups: (phone) => fetch(`${API_BASE_URL}/groups/phone/${phone}`),
    createGroup: (groupName, creatorPhone) => fetch(`${API_BASE_URL}/groups/createByPhone`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ groupName, creatorPhone })
    }),
    addMemberByPhone: (groupName, phone) => fetch(`${API_BASE_URL}/groups/addByPhone/${groupName}/${phone}`, { method: 'POST' }),

    // --- PAYMENT & CALCULATION ENDPOINTS (Uses Phone/GroupName) ---
    addPayment: (groupName, phone, data) => fetch(`${API_BASE_URL}/members/paymentByPhone/${groupName}/${phone}`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data)
    }),
    getBalances: (groupName) => fetch(`${API_BASE_URL}/calculate/balances/${groupName}`),
    getSettlements: (groupName) => fetch(`${API_BASE_URL}/calculate/settle/${groupName}`),
};