<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Book removed from library</title>
</head>
<body style="margin:0;padding:0;background-color:#f4f6f8;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;color:#1f2933;">
  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="padding:32px 0;">
    <tr>
      <td align="center">
        <table role="presentation" width="560" cellpadding="0" cellspacing="0"
               style="background:#ffffff;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.06);overflow:hidden;">
          <tr>
            <td style="background:#1971c2;padding:24px 32px;color:#ffffff;">
              <h1 style="margin:0;font-size:20px;font-weight:600;">Library Update</h1>
              <p style="margin:4px 0 0;font-size:14px;opacity:0.9;">A book has been removed from the catalog</p>
            </td>
          </tr>
          <tr>
            <td style="padding:32px;">
              <p style="margin:0 0 16px;font-size:16px;">Hello,</p>
              <p style="margin:0 0 24px;font-size:15px;line-height:1.5;">
                The following book is no longer available in our library:
              </p>
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0"
                     style="background:#f1f3f5;border-left:4px solid #1971c2;border-radius:4px;">
                <tr>
                  <td style="padding:16px 20px;">
                    <p style="margin:0 0 6px;font-size:13px;color:#6c757d;text-transform:uppercase;letter-spacing:0.5px;">Title</p>
                    <p style="margin:0 0 14px;font-size:17px;font-weight:600;color:#1f2933;">${title}</p>
                    <p style="margin:0 0 6px;font-size:13px;color:#6c757d;text-transform:uppercase;letter-spacing:0.5px;">ISBN</p>
                    <p style="margin:0;font-size:15px;font-family:'SFMono-Regular',Consolas,monospace;color:#1f2933;">${isbn}</p>
                  </td>
                </tr>
              </table>
              <p style="margin:28px 0 0;font-size:14px;color:#6c757d;line-height:1.5;">
                If you believe this was a mistake, please reach out to your library administrator.
              </p>
            </td>
          </tr>
          <tr>
            <td style="background:#f8f9fa;padding:16px 32px;text-align:center;border-top:1px solid #e9ecef;">
              <p style="margin:0;font-size:12px;color:#868e96;">
                &copy; ${.now?string("yyyy")} Library Management System
              </p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>
