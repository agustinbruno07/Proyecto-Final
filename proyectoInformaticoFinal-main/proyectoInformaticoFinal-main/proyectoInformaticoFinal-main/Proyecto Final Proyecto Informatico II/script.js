// busca en el documento el elemento con el id 'btn-carrito'
// y le agrega un "escuchador de eventos" (event listener)
// que detecta cuando el usuario hace clic en el boton
document.getElementById('btn-carrito').addEventListener('click', () => {
    // cuando se hace clic, muestra el mensaje
    alert('LicuaMax Pro añadida al carrito 🛒');
});

// Hace lo mismo pero con el botón de compra, busca el elemento con id 'btn-comprar'
document.getElementById('btn-comprar').addEventListener('click', () => {
    // cuando se hace clicl, muestra el mensaje
    alert('Redirigiendo al proceso de compra...');
});