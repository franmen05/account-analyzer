const CATEGORIES = [
    { key: 'commissions',   label: 'Comisiones',          accent: 'var(--violet)'  },
    { key: 'interest',      label: 'Intereses',           accent: 'var(--sky)'     },
    { key: 'nonPaymentFee', label: 'Mora',                accent: 'var(--rose)'    },
    { key: 'taxes',         label: 'Impuestos',           accent: 'var(--amber)'   },
    { key: 'userInterest',  label: 'Interés del usuario', accent: 'var(--emerald)' },
];

const money = (n) => 'RD$ ' + Number(n || 0).toLocaleString('es-DO', {
    minimumFractionDigits: 2, maximumFractionDigits: 2,
});

const esc = (s) => String(s).replace(/[&<>"]/g, (c) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

$(() => {
    const $results = $('#results');

    // ---- dropzone ----
    const $dz = $('#dropzone');
    const $input = $('#file');
    const $fileName = $('#fileName');

    $input.on('change', () => {
        const f = $input[0].files[0];
        if (f) {
            $fileName.text(f.name);
            $dz.addClass('has-file');
        } else {
            $dz.removeClass('has-file');
        }
    });
    ['dragenter', 'dragover'].forEach((ev) =>
        $dz.on(ev, (e) => { e.preventDefault(); $dz.addClass('is-drag'); }));
    ['dragleave', 'drop'].forEach((ev) =>
        $dz.on(ev, (e) => { e.preventDefault(); $dz.removeClass('is-drag'); }));

    // ---- states ----
    const showLoading = () => $results.html(
        '<div class="state"><div class="spinner"></div>' +
        '<h3>Analizando…</h3><p>Leyendo el estado de cuenta y agrupando deducciones.</p></div>');

    const showError = (msg) => $results.html(
        '<div class="state state--error">' +
        '<svg class="state__icon" width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="9"/><line x1="12" y1="8" x2="12" y2="13"/><line x1="12" y1="16.5" x2="12" y2="16.5"/></svg>' +
        '<h3>No se pudo analizar</h3><p>' + esc(msg) + '</p></div>');

    // ---- render dashboard ----
    const render = (data) => {
        const cards = CATEGORIES.map((c, i) => {
            const cat = data[c.key] || { total: 0, transactionsDesc: [] };
            const items = cat.transactionsDesc || [];
            const empty = !cat.total;
            const list = items.map((d) => '<li>' + esc(d) + '</li>').join('');
            return (
                '<div class="cat ' + (empty ? 'is-empty' : 'is-active') + '"' +
                ' style="--accent:' + c.accent + ';animation-delay:' + (i * 0.05) + 's">' +
                '<div class="cat__head"><span class="cat__dot"></span>' +
                '<span class="cat__name">' + c.label + '</span>' +
                (items.length ? '<span class="cat__count tnum">' + items.length + '</span>' : '') +
                '</div>' +
                '<div class="cat__amount tnum">' + (empty ? 'Sin movimientos' : money(cat.total)) + '</div>' +
                (items.length ? '<ul class="cat__list">' + list + '</ul>' : '') +
                '</div>'
            );
        }).join('');

        const out = data.userInterestOutTotal || { total: 0, transactionsDesc: [] };
        const outItems = out.transactionsDesc || [];
        const outCard = outItems.length ? (
            '<p class="section-label">Fuera del total</p>' +
            '<div class="cat-grid"><div class="cat is-active" style="--accent:var(--emerald)">' +
            '<div class="cat__head"><span class="cat__dot"></span>' +
            '<span class="cat__name">Interés del usuario</span>' +
            '<span class="cat__count tnum">' + outItems.length + '</span></div>' +
            '<div class="cat__amount tnum">' + money(out.total) + '</div>' +
            '<ul class="cat__list">' + outItems.map((d) => '<li>' + esc(d) + '</li>').join('') + '</ul>' +
            '</div></div>'
        ) : '';

        $results.html(
            '<div class="total-hero"><span class="total-hero__label">Total de deducciones</span>' +
            '<span class="total-hero__value tnum"><small>RD$</small>' +
            Number(data.total || 0).toLocaleString('es-DO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) +
            '</span></div>' +
            '<div class="cat-grid">' + cards + '</div>' + outCard);

        // expand/collapse transaction lists
        $results.find('.cat.is-active').on('click', function () { $(this).toggleClass('open'); });
    };

    // ---- submit ----
    const frm = $('#form');
    frm.on('submit', function (e) {
        e.preventDefault();
        if (!$input[0].files.length) { showError('Selecciona un archivo primero.'); return; }

        const $btn = $('#button').prop('disabled', true).text('Analizando…');
        showLoading();

        $.ajax({
            type: frm.attr('method'),
            url: frm.attr('action'),
            data: new FormData(frm[0]),
            processData: false,
            contentType: false,
            cache: false,
            success: (data) => {
                if (!data) { showError('El servidor no devolvió resultados.'); return; }
                render(data);
            },
            error: (xhr) => {
                const msg = (xhr.responseText && xhr.responseText.length < 300)
                    ? xhr.responseText
                    : 'Revisa el banco seleccionado, la contraseña y el formato del archivo.';
                showError(msg);
            },
            complete: () => $btn.prop('disabled', false).text('Analizar'),
        });
    });

    // ---- close ----
    $('#bClose').on('click', function (e) {
        e.preventDefault();
        if (!confirm('¿Cerrar Account Analyzer?')) return;
        $.ajax({
            type: 'GET',
            url: 'account/close',
            success: () => {},
            error: () => {
                document.body.innerHTML =
                    '<div class="state" style="min-height:100vh"><h3>Servicio cerrado</h3>' +
                    '<p>Account Analyzer se cerró correctamente. Puedes cerrar esta pestaña.</p></div>';
            },
        });
    });
});
