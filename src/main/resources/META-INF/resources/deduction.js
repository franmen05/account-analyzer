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

    const table = new DataTable('#dataTable-deductions', {
        ajax: 'http://localhost:8082/deduction/list',
        columns: [
            { data: 'id' },
            { data: 'type' },
            { data: 'description' },
            { data: null,
                defaultContent: '<button>X</button>',
                targets: -1
            }
        ]
    });

    table.on('click', 'button',  (e) =>{

        let data = table.row(e.target.closest('tr')).data();
        alert(data[0] + "'s salary is: " + data[2]);
    });

});