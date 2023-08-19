$(() => {

    const frm = $('#form');
    frm.submit(function (e) {
        e.preventDefault();

        $.ajax({
            type: frm.attr('method'),
            url: frm.attr('action'),
            data:  new FormData($(frm)[0]),
            processData: false,
            contentType: false,
            cache: false,
            success:  (data) => alert("Deducción creada con exito!"),
            error: (data) => {
                alert("Error al guardar  ")
                console.error(data)
            },
        });
    });
});