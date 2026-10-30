package com.example.mobilemhealthpay.utils

import android.content.Context
import android.net.Uri
import com.example.mobilemhealthpay.data.entity.RefundInfoTable
import java.io.BufferedWriter
import java.io.OutputStreamWriter

object DocxExporter {

    fun exportTransactionsToDocx(context: Context, uri: Uri, transactions: List<RefundInfoTable>): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream, "UTF-8")).use { writer ->
                    writer.write("<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" ")
                    writer.write("xmlns:w=\"urn:schemas-microsoft-com:office:word\" ")
                    writer.write("xmlns=\"http://www.w3.org/TR/REC-html40\">\n")
                    writer.write("<head>\n")
                    writer.write("<!--[if gte mso 9]><xml><w:WordDocument><w:View>Print</w:View><w:Zoom>100</w:Zoom></w:WordDocument></xml><![endif]-->\n")
                    writer.write("<style>\n")
                    writer.write("  table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; }\n")
                    writer.write("  th, td { border: 1px solid #dddddd; text-align: left; padding: 8px; }\n")
                    writer.write("  th { background-color: #f2f2f2; font-weight: bold; }\n")
                    writer.write("  tr:nth-child(even) { background-color: #fafafa; }\n")
                    writer.write("  h2 { font-family: Arial, sans-serif; color: #333333; text-align: center; }\n")
                    writer.write("</style>\n")
                    writer.write("</head>\n")
                    writer.write("<body>\n")
                    
                    writer.write("<h2>Liste des transactions ayant été effectuées</h2>\n")
                    writer.write("<table>\n")
                    writer.write("  <thead>\n")
                    writer.write("    <tr>\n")
                    writer.write("      <th>Numéros</th>\n")
                    writer.write("      <th>Montants</th>\n")
                    writer.write("      <th>Dates</th>\n")
                    writer.write("      <th>Type</th>\n")
                    writer.write("    </tr>\n")
                    writer.write("  </thead>\n")
                    writer.write("  <tbody>\n")
                    
                    for (tx in transactions) {
                        val typeStr = if (tx.isRefund == 1) "Remboursement" else "Virement"
                        writer.write("    <tr>\n")
                        writer.write("      <td>${tx.phoneNumber}</td>\n")
                        writer.write("      <td>${tx.amount}</td>\n")
                        writer.write("      <td>${tx.createdAt}</td>\n")
                        writer.write("      <td>$typeStr</td>\n")
                        writer.write("    </tr>\n")
                    }
                    
                    writer.write("  </tbody>\n")
                    writer.write("</table>\n")
                    writer.write("</body>\n")
                    writer.write("</html>\n")
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
