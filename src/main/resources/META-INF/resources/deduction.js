const TYPE_META = {
    COMMISSIONS:             { label: 'Comisiones',                  accent: 'var(--violet)'  },
    TAXES:                   { label: 'Impuestos',                   accent: 'var(--amber)'   },
    INTEREST:                { label: 'Intereses',                   accent: 'var(--sky)'     },
    NON_PAYMENT_FEE:         { label: 'Mora',                        accent: 'var(--rose)'    },
    USER_INTEREST:           { label: 'Interés del usuario',         accent: 'var(--emerald)' },
    USER_INTEREST_OUT_TOTAL: { label: 'Interés usuario (fuera)',     accent: 'var(--emerald)' },
};

const esc = (s) => String(s).replace(/[&<>"]/g, (c) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

$(() => {
    const $rows = $('#rows');
    const $empty = $('#emptyRules');
    const $count = $('#ruleCount');

    const load = () => {
        $.getJSON('deduction/list', (res) => {
            const data = (res && res.data) || [];
            $count.text(data.length);
            $empty.toggle(data.length === 0);

            $rows.html(data.map((d) => {
                const meta = TYPE_META[d.type] || { label: d.type, accent: 'var(--text-muted)' };
                return (
                    '<tr>' +
                    '<td class="num">' + esc(d.id) + '</td>' +
                    '<td><span class="tag" style="--accent:' + meta.accent + '">' + esc(meta.label) + '</span></td>' +
                    '<td>' + esc(d.description) + '</td>' +
                    '<td style="text-align:right">' +
                    '<button class="icon-btn" data-id="' + esc(d.id) + '" title="Eliminar">' +
                    '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M3 6h18M8 6V4h8v2m-9 0v14a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1V6"/></svg>' +
                    '</button></td>' +
                    '</tr>'
                );
            }).join(''));
        });
    };

    // create
    const frm = $('#form');
    frm.on('submit', function (e) {
        e.preventDefault();
        const $btn = $('#button-send').prop('disabled', true).text('Creando…');
        $.ajax({
            type: frm.attr('method'),
            url: frm.attr('action'),
            data: new FormData(frm[0]),
            processData: false,
            contentType: false,
            cache: false,
            success: () => { $('#description').val(''); load(); },
            error: (xhr) => alert('No se pudo crear la regla. ' + (xhr.responseText || '')),
            complete: () => $btn.prop('disabled', false).text('Crear regla'),
        });
    });

    // delete (event delegation)
    $rows.on('click', '.icon-btn', function () {
        const id = $(this).data('id');
        if (!confirm('¿Eliminar la regla #' + id + '?')) return;
        $.ajax({
            type: 'DELETE',
            url: 'deduction/' + id,
            success: load,
            error: () => alert('No se pudo eliminar el registro.'),
        });
    });

    load();
});
