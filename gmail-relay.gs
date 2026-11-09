// Script de Google Apps Script: recibe los correos de la app por HTTPS
// y los envía con TU cuenta de Gmail (gratis, hasta ~100 correos al día).
//
// 1. Entra en https://script.google.com > "Nuevo proyecto" y pega este código.
// 2. Cambia SECRET por una clave larga inventada por ti (la misma que pondrás en MAIL_RELAY_SECRET).
// 3. Implementar > Nueva implementación > tipo "Aplicación web"
//      Ejecutar como: Yo   |   Quién tiene acceso: Cualquier usuario
// 4. Autoriza los permisos y copia la URL que termina en /exec (va en MAIL_RELAY_URL).

const SECRET = 'CAMBIA-ESTA-CLAVE';

function doPost(e) {
  try {
    const d = JSON.parse(e.postData.contents);
    if (d.secret !== SECRET) return respond({ ok: false, error: 'Clave del relay incorrecta' });
    MailApp.sendEmail({
      to: d.to,
      subject: d.subject,
      body: d.body,
      htmlBody: d.htmlBody,
      name: d.name || 'Sistema de Correos'
    });
    return respond({ ok: true });
  } catch (err) {
    return respond({ ok: false, error: String(err) });
  }
}

function respond(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
