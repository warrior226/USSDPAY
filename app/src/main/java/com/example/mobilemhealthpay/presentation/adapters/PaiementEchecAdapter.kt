package com.example.mobilemhealthpay.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilemhealthpay.R
import com.example.mobilemhealthpay.utils.Constant

class PaiementEchecAdapter(private  val items: List<TransactionInfoItem>): RecyclerView.Adapter<PaiementEchecAdapter.PaiementEchecViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaiementEchecAdapter.PaiementEchecViewHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.payment_item,parent,false)
        return PaiementEchecViewHolder(itemView)

    }

    override fun onBindViewHolder(
        holder: PaiementEchecAdapter.PaiementEchecViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.beneficiaireNom.text = holder.itemView.context.getString(R.string.beneficiaire, item.numero)
        holder.montant.text = holder.itemView.context.getString(R.string.montant_transfere, item.montant.toString())
        holder.operateur.text = holder.itemView.context.getString(R.string.operateur, item.operateur)
        holder.dateOperation.text = holder.itemView.context.getString(R.string.date_operation, item.dateCreation)
        holder.status.text="Echouée"
        holder.status.setTextColor(
            ContextCompat.getColor(holder.itemView.context, R.color.statusFailedColor)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }


    class PaiementEchecViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){

        val beneficiaireNom: TextView=itemView.findViewById(R.id.beneficiaire)
        val montant: TextView = itemView.findViewById(R.id.montant)
        val operateur:TextView=itemView.findViewById(R.id.operateur)
        val dateOperation: TextView=itemView.findViewById(R.id.date)
        val status: TextView=itemView.findViewById(R.id.status)
    }


}