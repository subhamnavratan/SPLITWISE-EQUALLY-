// app.js (Entry point and logic)

import { API } from './api.js';

const appContainer = document.getElementById('app-container');
// Load session data from localStorage
let USER_PHONE = localStorage.getItem('userPhone');
let USER_ID = localStorage.getItem('userId');
let USER_NAME = localStorage.getItem('userName');
let CURRENT_GROUP = localStorage.getItem('currentGroup'); // Persist current group

// --- Helper Functions ---
const showMessage = (msg, type = 'error') => {
    const el = document.createElement('p');
    el.className = `message message-${type}`;
    el.textContent = msg;
    return el;
};

const render = (htmlContent) => { appContainer.innerHTML = htmlContent; };

const logout = () => {
    localStorage.clear();
    USER_PHONE = USER_ID = USER_NAME = CURRENT_GROUP = null;
    renderAuth();
};

// --- View Rendering Functions (Auth, Group List, Details) ---

const renderAuth = () => {
    render(`
        <h1>Splitwise Authentication</h1>
        <div class="auth-form" id="authForm">
            <h2 id="authTitle">Login</h2>
            <form id="loginForm">
                <input type="text" id="authIdentifier" placeholder="Phone or Email" required>
                <button type="submit">Log In</button>
            </form>
            <p><a href="#" id="switchAuth">Need to register? Sign Up</a></p>
            <div id="authMessage"></div>
        </div>
    `);
    document.getElementById('authForm').addEventListener('submit', handleAuthSubmit);
    document.getElementById('switchAuth').addEventListener('click', switchAuthMode);
};

const renderGroupDashboard = async () => {
    if (!USER_PHONE) return logout();

    let groups = [];
    try {
        const response = await API.getGroups(USER_PHONE);
        if (response.ok) {
            groups = await response.json();
        }
    } catch (e) { /* Handle group load error */ }

    render(`
        <h1>Group Dashboard</h1>
        <div class="dashboard-header">
            <p>Welcome, ${USER_NAME}!</p>
            <button onclick="window.logout()">Logout</button>
        </div>

        <div class="grid-layout">
            <div class="card md:col-span-2">
                <h2>Your Groups</h2>
                <div id="groupList">
                    ${groups.length > 0
                        ? groups.map(g => `<div class="group-item" data-name="${g.groupName}" onclick="window.selectGroup('${g.groupName}')">${g.groupName}</div>`).join('')
                        : '<p>No groups found. Create one!</p>'}
                </div>
            </div>

            <div class="card">
                <h2>Create Group</h2>
                <form id="createGroupForm">
                    <input type="text" id="newGroupName" placeholder="Enter group name" required>
                    <button type="submit">Create Group</button>
                </form>
                <div id="createGroupMessage"></div>
            </div>
        </div>
    `);
    document.getElementById('createGroupForm').addEventListener('submit', handleCreateGroup);
};

const renderGroupDetails = async () => {
    if (!CURRENT_GROUP || !USER_PHONE) return logout();

    let balances = [];
    let settlements = [];

    try {
        const [balancesRes, settlementsRes] = await Promise.all([
            API.getBalances(CURRENT_GROUP),
            API.getSettlements(CURRENT_GROUP)
        ]);

        if (balancesRes.ok) balances = await balancesRes.json();
        if (settlementsRes.ok) settlements = await settlementsRes.json();

    } catch (e) {
        console.error("Error loading details:", e);
    }

    render(`
        <h1>${CURRENT_GROUP} Details</h1>
        <div class="dashboard-header">
            <button onclick="window.renderGroupDashboard()">← Back to Groups</button>
            <p>Logged in as: ${USER_NAME}</p>
        </div>

        <div class="grid-layout">

            <div class="card">
                <h2>Net Balances</h2>
                <div id="balancesList">
                    ${balances.map(b => {
                        const balance = b.totalPaidAmount; // This holds the calculated debt/credit
                        const statusClass = balance > 0 ? 'positive' : balance < 0 ? 'negative' : 'zero';
                        const statusText = balance > 0
                            ? `Owed: <span class="amount-due">₹${balance}</span>`
                            : balance < 0
                            ? `Owes: <span class="amount-owed">₹${-balance}</span>`
                            : 'Settled';

                        return `
                            <div class="balance-item ${statusClass}">
                                <span>${b.name}</span>
                                <span>${statusText}</span>
                            </div>`;
                    }).join('')}
                </div>
            </div>

            <div class="card">
                <h2>Settlement Plan</h2>
                <div id="settlementList">
                    ${settlements.length > 0
                        ? settlements.map(s => `
                            <div class="settle-item">
                                ➡️ ${s.payerName} pays ${s.receiverName}
                                <span class="amount-due">₹${s.amount}</span>
                            </div>`).join('')
                        : '<p>Group is fully settled! 🎉</p>'}
                </div>
            </div>

            <div class="card">
                <h2>Record Payment</h2>
                <div id="actionMessage"></div>

                <form id="addPaymentForm" data-phone="${USER_PHONE}">
                    <input type="number" id="paymentAmount" placeholder="Amount ($)" required>
                    <input type="text" id="paymentDescription" placeholder="Description" required>
                    <button type="submit">I Paid This</button>
                </form>

                <h3 class="mt-4">Add Member by Phone</h3>
                <form id="addMemberForm">
                    <input type="number" id="memberPhone" placeholder="Member's phone number" required>
                    <button type="submit">Add Member</button>
                </form>
            </div>
        </div>
    `);

    document.getElementById('addPaymentForm').addEventListener('submit', handleAddPayment);
    document.getElementById('addMemberForm').addEventListener('submit', handleAddMember);
};


// --- Controller and Event Handlers ---

let isRegisterMode = false;
const switchAuthMode = (e) => { /* ... (Logic remains the same as previous response) ... */ };
const handleAuthSubmit = async (e) => { /* ... (Logic remains the same as previous response) ... */ };
const handleCreateGroup = async (e) => {
    e.preventDefault();
    const groupName = document.getElementById('newGroupName').value;
    const messageDiv = document.getElementById('createGroupMessage');
    messageDiv.innerHTML = '';

    try {
        const response = await API.createGroup(groupName, USER_PHONE);
        const data = await response.json();

        if (!response.ok) throw new Error(data.message || 'Failed to create group.');

        messageDiv.appendChild(showMessage(`Group "${groupName}" created!`, 'success'));
        renderGroupDashboard();

    } catch (error) {
        messageDiv.appendChild(showMessage(error.message || 'Group creation failed.'));
    }
};

const handleAddPayment = async (e) => {
    e.preventDefault();
    const form = e.target;
    const amount = parseInt(document.getElementById('paymentAmount').value);
    const description = document.getElementById('paymentDescription').value;
    const messageDiv = document.getElementById('actionMessage');
    messageDiv.innerHTML = '';

    if (isNaN(amount) || amount <= 0) return messageDiv.appendChild(showMessage('Invalid amount.'));

    try {
        const paymentData = { amount, description };
        // Uses the logged-in user's phone number
        const response = await API.addPayment(CURRENT_GROUP, USER_PHONE, paymentData);

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to record payment.');
        }

        messageDiv.appendChild(showMessage(`Payment recorded!`, 'success'));
        form.reset();
        renderGroupDetails(); // Refresh details
    } catch (error) {
        messageDiv.appendChild(showMessage(error.message || 'Payment failed. Check your network/membership.'));
        console.error(error);
    }
};

const handleAddMember = async (e) => { /* ... (Logic remains the same as previous response) ... */ };

// --- Window Exposure and Initialization ---
window.selectGroup = (groupName) => {
    CURRENT_GROUP = groupName;
    localStorage.setItem('currentGroup', groupName);
    renderGroupDetails();
};

window.logout = logout;
window.renderGroupDashboard = renderGroupDashboard; // Expose for back button
window.switchAuthMode = switchAuthMode;

// Initial check and routing
document.addEventListener('DOMContentLoaded', () => {
    if (USER_PHONE) {
        renderGroupDashboard();
    } else {
        renderAuth();
    }
});