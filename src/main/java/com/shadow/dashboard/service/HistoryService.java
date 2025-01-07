package com.shadow.dashboard.service;

import com.shadow.dashboard.models.History;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class HistoryService {

    public String formatadorData(History history) {
        if (history != null && history.getCreated() != null) {
            Calendar calendars = Calendar.getInstance();
            calendars.setTime(history.getCreated());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(calendars.getTime());
        }
        return null;
    }

    // Método para calcular a data de pagamento
    public String calculadorDeMeses(History history) {
        if (history != null && history.getCreated() != null && history.getParcelamento() > 0) {
            // Obtém a data de criação (início)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(history.getCreated());

            // Adiciona o número de meses de acordo com o parcelamento
            calendar.add(Calendar.MONTH, history.getParcelamento());

            // Obtemos a nova data (dia de pagamento)
            Date dataPagamento = calendar.getTime();

            // Formatar a data no formato "dd MM yyyy"
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dataPagamento);  // Retorna a data formatada
        }
        return null;  // Se algum dado estiver faltando, retorna null
    }


}
