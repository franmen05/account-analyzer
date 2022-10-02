$(() => {

    $('#file-uploader').dxFileUploader({
        selectButtonText: 'Select File',
        name: 'file',
        labelText: '',
        uploadMode: 'useForm',
        multiple: false,
        allowedFileExtensions: ['.csv'],
    });

    $('#button').dxButton({
        text: 'Analyze ',
        type: 'success',
        onClick() {
            // const confirm=DevExpress.ui.dialog.confirm('Is file correct?.')
            // confirm.done((dialogResult)=> dialogResult?$("#form").submit():'');
            $("#form").submit();
        },
    });

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
                console.info('Submission was successful.');
                console.debug(data);

                addTransactionsDetail($('#commissions'),$('#commissionsTrans'),data.commissions);
                addTransactionsDetail($('#interest'),$('#interestTrans'),data.interest);
                addTransactionsDetail($('#taxes'),$('#taxesTrans'),data.taxes);
                addTransactionsDetail($('#nonPaymentFee'),$('#nonPaymentFeeTrans'),data.nonPaymentFee);

                $('#total').text(data.total)
            },
            error: (data) => {
                console.debug('An error occurred.');
                console.error(data);
            },
        });
    });
});

const addTransactionsDetail= (parentContainer,idContainer, data)=> {
    idContainer.empty();
    if (data.total != 0) {

        parentContainer.show()
        data.transactionsDesc.forEach((desc) => idContainer.append(`<li>${desc}</li> `))
        idContainer.append(`<br> `)
        idContainer.append(`<span class="text-bold"> Total : $ ${data.total}</span>`)

    }else{
        parentContainer.hide()

    }
}