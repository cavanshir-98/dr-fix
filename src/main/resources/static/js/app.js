const API = '/api';
let currentStep = 1;
let selectedAppliance = '';
let allServices = [];
let loggedInCustomer = null;

document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initMobileMenu();
    initFAQ();
    initServiceTabs();
    loadServices();
    initBookingForm();
    initContactForm();
    initDatePicker();
    initAccountNav().then(customer => { loggedInCustomer = customer; });
});

/* ===== Navbar scroll ===== */
function initNavbar() {
    const navbar = document.getElementById('navbar');
    window.addEventListener('scroll', () => {
        navbar.classList.toggle('scrolled', window.scrollY > 20);
    });
}

function initMobileMenu() {
    const toggle = document.getElementById('mobileToggle');
    const links = document.getElementById('navLinks');
    if (!toggle || !links) return;
    toggle.addEventListener('click', () => links.classList.toggle('open'));
    links.querySelectorAll('a').forEach(a => {
        a.addEventListener('click', () => links.classList.remove('open'));
    });
}

/* ===== FAQ Accordion ===== */
function initFAQ() {
    document.querySelectorAll('.faq-question').forEach(btn => {
        btn.addEventListener('click', () => {
            const item = btn.parentElement;
            const wasOpen = item.classList.contains('open');
            document.querySelectorAll('.faq-item').forEach(i => i.classList.remove('open'));
            if (!wasOpen) item.classList.add('open');
        });
    });
}

/* ===== Services ===== */
async function loadServices(category = '') {
    const url = category ? `${API}/services?category=${category}` : `${API}/services`;
    try {
        const res = await fetch(url);
        const json = await res.json();
        allServices = json.data || [];
        renderServices(allServices);
        renderApplianceSelect(allServices);
    } catch (e) {
        console.error('Failed to load services', e);
    }
}

function renderServices(services) {
    const grid = document.getElementById('servicesGrid');
    grid.innerHTML = services.map(s => `
        <div class="service-card" onclick="selectApplianceAndBook('${s.name}')">
            <div class="service-icon">${s.icon}</div>
            <h3>${s.name}</h3>
            <p>${s.description}</p>
            <span class="service-tag">${s.category}</span>
        </div>
    `).join('');
}

function renderApplianceSelect(services) {
    const container = document.getElementById('applianceSelect');
    container.innerHTML = services.map(s => `
        <div class="appliance-option" data-name="${s.name}" onclick="selectAppliance(this, '${s.name}')">
            <div class="appliance-icon">${s.icon}</div>
            <div class="appliance-name">${s.name}</div>
        </div>
    `).join('');
}

function initServiceTabs() {
    document.querySelectorAll('#serviceTabs .tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('#serviceTabs .tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            loadServices(tab.dataset.category);
        });
    });
}

function selectAppliance(el, name) {
    document.querySelectorAll('.appliance-option').forEach(o => o.classList.remove('selected'));
    el.classList.add('selected');
    selectedAppliance = name;
    document.getElementById('step1Next').disabled = false;
}

function selectApplianceAndBook(name) {
    openBookingModal();
    setTimeout(() => {
        const option = document.querySelector(`.appliance-option[data-name="${name}"]`);
        if (option) selectAppliance(option, name);
    }, 100);
}

/* ===== Booking Modal ===== */
async function openBookingModal() {
    loggedInCustomer = await fetchLoggedInCustomer();
    if (!loggedInCustomer) {
        showToast('Please register before booking', 'error');
        setTimeout(() => {
            window.location.href = '/register.html';
        }, 1200);
        return;
    }

    document.getElementById('bookingModal').classList.add('active');
    document.body.style.overflow = 'hidden';
    resetBookingForm();
    applyCustomerProfileToBookingForm(loggedInCustomer);
}

function closeBookingModal() {
    document.getElementById('bookingModal').classList.remove('active');
    document.body.style.overflow = '';
}

function resetBookingForm() {
    currentStep = 1;
    selectedAppliance = '';
    document.getElementById('bookingForm').reset();
    document.getElementById('bookingForm').hidden = false;
    document.getElementById('bookingSuccess').hidden = true;
    document.getElementById('bookingSteps').hidden = false;
    document.getElementById('viewBookingsBtn').hidden = true;
    const whatsappBtn = document.getElementById('whatsappNotifyBtn');
    if (whatsappBtn) {
        whatsappBtn.hidden = true;
        whatsappBtn.classList.remove('pulse');
    }
    document.getElementById('preferredTime').value = '';
    document.getElementById('timeSlots').innerHTML = '<p class="slots-hint">Select a date to see available times</p>';
    document.querySelectorAll('.appliance-option').forEach(o => o.classList.remove('selected'));
    document.getElementById('step1Next').disabled = true;
    document.getElementById('step3Next').disabled = true;
    updateSteps(1);
    showPanel(1);
}

function nextStep(step) {
    if (step === 3 && !validateStep2()) return;
    if (step === 4) {
        if (!document.getElementById('preferredTime').value) {
            showToast('Please select a time slot', 'error');
            return;
        }
        renderSummary();
    }
    currentStep = step;
    updateSteps(step);
    showPanel(step);
}

function prevStep(step) {
    currentStep = step;
    updateSteps(step);
    showPanel(step);
}

function updateSteps(active) {
    document.querySelectorAll('.booking-steps .step').forEach(s => {
        const num = parseInt(s.dataset.step);
        s.classList.remove('active', 'completed');
        if (num === active) s.classList.add('active');
        else if (num < active) s.classList.add('completed');
    });
}

function showPanel(num) {
    document.querySelectorAll('.booking-panel').forEach(p => {
        p.classList.toggle('active', parseInt(p.dataset.panel) === num);
    });
}

function validateStep2() {
    const form = document.getElementById('bookingForm');
    const fields = ['fullName', 'phone', 'email', 'address', 'city', 'state', 'zipCode'];
    for (const name of fields) {
        const input = form.querySelector(`[name="${name}"]`);
        if (!input.value.trim()) {
            input.focus();
            showToast('Please fill in all required fields', 'error');
            return false;
        }
    }
    return true;
}

function renderSummary() {
    const form = document.getElementById('bookingForm');
    const fd = new FormData(form);
    const date = fd.get('preferredDate');
    const time = fd.get('preferredTime');

    document.getElementById('bookingSummary').innerHTML = `
        <div class="summary-row"><span class="label">Appliance</span><span class="value">${selectedAppliance}</span></div>
        <div class="summary-row"><span class="label">Name</span><span class="value">${fd.get('fullName')}</span></div>
        <div class="summary-row"><span class="label">Phone</span><span class="value">${fd.get('phone')}</span></div>
        <div class="summary-row"><span class="label">Email</span><span class="value">${fd.get('email')}</span></div>
        <div class="summary-row"><span class="label">Address</span><span class="value">${fd.get('address')}, ${fd.get('city')}, ${fd.get('state')} ${fd.get('zipCode')}</span></div>
        <div class="summary-row"><span class="label">Date</span><span class="value">${formatDate(date)}</span></div>
        <div class="summary-row"><span class="label">Time</span><span class="value">${formatTime(time)}</span></div>
        ${fd.get('description') ? `<div class="summary-row"><span class="label">Issue</span><span class="value">${fd.get('description')}</span></div>` : ''}
    `;
}

function initDatePicker() {
    const input = document.getElementById('preferredDate');
    const today = new Date();
    input.min = today.toISOString().split('T')[0];

    const max = new Date();
    max.setDate(max.getDate() + 30);
    input.max = max.toISOString().split('T')[0];

    input.addEventListener('change', () => loadTimeSlots(input.value));
}

async function loadTimeSlots(date) {
    const container = document.getElementById('timeSlots');
    container.innerHTML = '<p class="slots-loading">Loading available slots...</p>';
    document.getElementById('preferredTime').value = '';
    document.getElementById('step3Next').disabled = true;

    try {
        const res = await fetch(`${API}/bookings/slots?date=${date}`);
        const json = await res.json();
        const slots = json.data || [];

        if (slots.length === 0) {
            container.innerHTML = '<p class="slots-hint">No slots available for this date. Please choose another date.</p>';
            return;
        }

        container.innerHTML = slots.map(slot => `
            <div class="time-slot" onclick="selectTimeSlot(this, '${slot}')">${formatTime(slot)}</div>
        `).join('');
    } catch (e) {
        container.innerHTML = '<p class="slots-hint">Failed to load slots. Please try again.</p>';
    }
}

function selectTimeSlot(el, time) {
    document.querySelectorAll('.time-slot').forEach(s => s.classList.remove('selected'));
    el.classList.add('selected');
    document.getElementById('preferredTime').value = time;
    document.getElementById('step3Next').disabled = false;
}

function initBookingForm() {
    document.getElementById('bookingForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('submitBooking');
        btn.disabled = true;
        btn.textContent = 'Confirming...';

        const form = document.getElementById('bookingForm');
        const fd = new FormData(form);

        const payload = {
            fullName: fd.get('fullName'),
            phone: fd.get('phone'),
            email: fd.get('email'),
            address: fd.get('address'),
            city: fd.get('city'),
            state: fd.get('state'),
            zipCode: fd.get('zipCode'),
            applianceType: selectedAppliance,
            description: fd.get('description') || '',
            preferredDate: fd.get('preferredDate'),
            preferredTime: fd.get('preferredTime')
        };

        try {
            const res = await fetch(`${API}/bookings`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify(payload)
            });
            const json = await res.json();

            if (res.status === 401 || res.status === 403) {
                showToast('Please register and sign in to book', 'error');
                setTimeout(() => { window.location.href = '/register.html'; }, 1200);
                return;
            }

            if (json.success) {
                showBookingSuccess(json.data);
            } else {
                const msg = json.errors
                    ? Object.values(json.errors).flat().join(', ')
                    : json.message;
                showToast(msg, 'error');
            }
        } catch (err) {
            showToast('Failed to submit booking. Please try again.', 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Confirm Booking';
        }
    });

    document.getElementById('bookingModal').addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) closeBookingModal();
    });
}

function showBookingSuccess(data) {
    document.getElementById('bookingForm').hidden = true;
    document.getElementById('bookingSteps').hidden = true;
    const success = document.getElementById('bookingSuccess');
    success.hidden = false;

    document.getElementById('confirmationDetails').innerHTML = `
        <div class="confirmation-code">${data.confirmationCode}</div>
        <div class="summary-row"><span class="label">Appliance</span><span class="value">${data.applianceType}</span></div>
        <div class="summary-row"><span class="label">Date</span><span class="value">${formatDate(data.preferredDate)}</span></div>
        <div class="summary-row"><span class="label">Time</span><span class="value">${formatTime(data.preferredTime)}</span></div>
        <div class="summary-row"><span class="label">Address</span><span class="value">${data.address}, ${data.city}, ${data.state}</span></div>
    `;

    const whatsappBtn = document.getElementById('whatsappNotifyBtn');
    const whatsappHint = document.getElementById('whatsappHint');

    if (data.ownerNotified || data.whatsappAutoSent) {
        if (whatsappBtn) whatsappBtn.hidden = true;
        if (whatsappHint) {
            const via = data.notifyChannel === 'telegram' ? 'Telegram' : 'WhatsApp';
            whatsappHint.textContent = `Booking confirmed! We notified our team via ${via}.`;
        }
        showToast('Booking confirmed — notification sent', 'success');
    } else if (whatsappBtn && data.whatsappUrl) {
        whatsappBtn.href = data.whatsappUrl;
        whatsappBtn.hidden = false;
        whatsappBtn.classList.add('pulse');
        if (whatsappHint) {
            whatsappHint.innerHTML = 'Please tap the button below to open WhatsApp and press <strong>Send</strong> so we receive your booking.';
        }
        showToast('Booking confirmed — please send the WhatsApp message', 'success');
        openWhatsAppLink(data.whatsappUrl);
    } else {
        if (whatsappBtn) whatsappBtn.hidden = true;
        showToast('Booking saved successfully', 'success');
    }

    const accountBtn = document.getElementById('viewBookingsBtn');
    if (accountBtn) accountBtn.hidden = false;
}

function openWhatsAppLink(url) {
    const link = document.createElement('a');
    link.href = url;
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
    document.body.appendChild(link);
    link.click();
    link.remove();
}

/* ===== Contact Form ===== */
function initContactForm() {
    document.getElementById('contactForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const feedback = document.getElementById('contactFeedback');
        const form = e.target;
        const fd = new FormData(form);

        const payload = {
            fullName: fd.get('fullName'),
            phone: fd.get('phone'),
            email: fd.get('email'),
            message: fd.get('message')
        };

        try {
            const res = await fetch(`${API}/contact`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();

            if (json.success) {
                feedback.className = 'form-feedback success';
                feedback.textContent = 'Message sent! We\'ll get back to you soon.';
                form.reset();
            } else {
                feedback.className = 'form-feedback error';
                feedback.textContent = json.message || 'Failed to send message.';
            }
        } catch (err) {
            feedback.className = 'form-feedback error';
            feedback.textContent = 'Failed to send message. Please try again.';
        }
    });
}

/* ===== Utilities ===== */
function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
}

function formatTime(timeStr) {
    if (!timeStr) return '';
    const [h, m] = timeStr.split(':');
    const hour = parseInt(h);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const h12 = hour % 12 || 12;
    return `${h12}:${m} ${ampm}`;
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    setTimeout(() => toast.classList.remove('show'), 4000);
}

function applyCustomerProfileToBookingForm(profile) {
    const form = document.getElementById('bookingForm');
    if (!form || !profile) return;

    const fullName = form.querySelector('[name="fullName"]');
    const phone = form.querySelector('[name="phone"]');
    const email = form.querySelector('[name="email"]');

    if (fullName) fullName.value = profile.fullName;
    if (phone) phone.value = profile.phone;
    if (email) email.value = profile.email;
}

async function loadCustomerProfileForBooking() {
    if (!loggedInCustomer) {
        loggedInCustomer = await fetchLoggedInCustomer();
    }
    applyCustomerProfileToBookingForm(loggedInCustomer);
}
