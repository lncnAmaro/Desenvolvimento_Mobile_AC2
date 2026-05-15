package com.example.ac2;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LivroAdapter extends RecyclerView.Adapter<LivroAdapter.ViewHolder>{

    private List<Livro> livros;

    public interface OnItemClickListener {
        void onItemClick(Livro livro);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public LivroAdapter(List<Livro> livros) {
        this.livros = livros;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Livro p = livros.get(pos);

        holder.titulo.setText(p.getTitulo());
        holder.autor.setText(p.getAutor());
        holder.genero.setText(p.getGenero());
        holder.anoPubli.setText(p.getAnoPubli());
        holder.status.setText(p.getStatus());
        holder.favorito.setChecked(p.getFavorito());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(p);
            }
        });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            private long lastClickTime = 0;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime < 300) {
                        deletarLivro(p.getId(), holder.getAdapterPosition(), v);
                    }
                    lastClickTime = currentTime;
                }
                return false;
            }
        });
    }

    private void deletarLivro(String idDocumento, int position, View view) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid(); // se quiser usar

            FirebaseFirestore.getInstance().collection("produtos")
                    .document(idDocumento)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        livros.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(view.getContext(), "Livro deletado!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(view.getContext(), "Erro ao deletar", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(view.getContext(), "Você precisa estar logado para realizar essa ação", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return livros.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox favorito;
        TextView titulo, autor, genero,anoPubli, status;
        public ViewHolder(View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.edtTitulo);
            autor = itemView.findViewById(R.id.edtAutor);
            genero = itemView.findViewById(R.id.spnGenero);
            anoPubli = itemView.findViewById(R.id.edtAno);
            status = itemView.findViewById(R.id.spnStatus);
        }
    }
}
