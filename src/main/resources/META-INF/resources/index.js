$(() => {

    $('#file-uploader').dxFileUploader({
        selectButtonText: 'Select File',
        name: 'file',
        labelText: '',
        uploadMode: 'useForm',
        multiple: false,
        allowedFileExtensions: ['.csv','.pdf','.PDF'],
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
                addTransactionsDetail($('#userInterest'),$('#userInterestTrans'),data.userInterest);
                addTransactionsDetail($('#userInterestOutTotal'),$('#userInterestOutTotalTrans'),data.userInterestOutTotal);
                let formattedNumber = new Intl.NumberFormat('en-US').format(data.total);

                $('#total').text(formattedNumber)
            },
            error: (data) => {
                console.debug('An error occurred.');
                console.error(data);
            },
        });
    });

    $('#bClose').click(function (e) {
        e.preventDefault();

        $.ajax({
            type: 'GET',
            url:'account/close',
            success:  () => alert("Error al intentar cerrar servicio"),
            error: (data) => {
                alert("Account Analyzer fue cerrado correctamente")
                $('#mainContainer').text("El servicio fue cerrado ")
            },
        });
    });
});

const addTransactionsDetail= (parentContainer,idContainer, data)=> {
    idContainer.empty();
    if (data.total !== 0) {

        parentContainer.show()
        data.transactionsDesc.forEach((desc) => idContainer.append(`<li>${desc}</li> `))
        idContainer.append(`<br> `)
        idContainer.append(`<span class="text-bold"> Total : $  ${new Intl.NumberFormat('en-US').format(data.total)}</span>`)

    }else{
        parentContainer.hide()

    }
}