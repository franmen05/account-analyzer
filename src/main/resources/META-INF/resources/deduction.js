const buildDataTable = ()=> {
    return new DataTable('#dataTable-deductions', {
        ajax: 'deduction/list',
        columns: [
            {data: 'id'},
            {data: 'type'},
            {data: 'description'},
            {
                data: null,
                defaultContent: '<button class=" btn btn-danger">X</button>',
                targets: -1
            }
        ]
    });
}

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
            success:  (data) => {
                alert("Deducción creada con exito!");
                location.reload();
            },
            error: (data) => {
                alert("Error al guardar  ")
                console.error(data)
            },
        });
    });

    let table = buildDataTable();
    table.on('click', 'button',  (e) =>{

        let data = table.row(e.target.closest('tr')).data();
        if(confirm(` Seguro que Desea Eliminar el ID ${data.id} ?` )){
            $.ajax({
                type: 'DELETE',
                url:`deduction/${data.id}`,
                success:  () => location.reload()  ,
                error: (data) => {
                    alert("No Se puedo eliminar el resgistro   ")
                    console.error(data)
                },
            });
        }
    });

});