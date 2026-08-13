const AUTH_API = '/api';

async function fetchLoggedInCustomer() {
    try {
        const res = await fetch(`${AUTH_API}/account/me`, { credentials: 'same-origin' });
        if (!res.ok) return null;
        const json = await res.json();
        return json.success ? json.data : null;
    } catch (e) {
        return null;
    }
}

function renderAccountNav(customer) {
    const nav = document.getElementById('accountNav');
    if (!nav) return;

    if (customer) {
        nav.innerHTML = `
            <a href="/account.html" class="nav-auth-link">My Bookings</a>
            <a href="/logout" class="nav-auth-link">Logout</a>
        `;
        document.querySelectorAll('.nav-guest-only').forEach(el => { el.hidden = true; });
        document.querySelectorAll('.nav-member-only').forEach(el => { el.hidden = false; });
    } else {
        nav.innerHTML = `
            <a href="/account/login.html" class="nav-auth-link">Sign In</a>
            <a href="/register.html" class="btn btn-outline btn-sm">Sign Up</a>
        `;
        document.querySelectorAll('.nav-guest-only').forEach(el => { el.hidden = false; });
        document.querySelectorAll('.nav-member-only').forEach(el => { el.hidden = true; });
    }

    nav.classList.add('ready');
}

async function initAccountNav() {
    const customer = await fetchLoggedInCustomer();
    window.loggedInCustomer = customer;
    renderAccountNav(customer);
    return customer;
}

async function redirectIfAuthenticated(redirectTo = '/account.html') {
    const customer = await fetchLoggedInCustomer();
    if (customer) {
        window.location.href = redirectTo;
    }
}
