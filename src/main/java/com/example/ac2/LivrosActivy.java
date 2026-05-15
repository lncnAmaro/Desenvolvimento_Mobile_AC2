package com.example.ac2;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LivrosActivy extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText edtTitulo, edtAutor, edtAno;
    private Spinner spnStatus, spnGenero;
    private CheckBox checkBox;
    private RecyclerView lista;
    private List<Livro> listaLivros = new ArrayList<>();
    private LivroAdapter adapter;
    private Livro livroEditando = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_livros_activy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        edtTitulo = findViewById(R.id.edtTitulo);
        edtAutor = findViewById(R.id.edtAutor);
        spnGenero = findViewById(R.id.spnGenero);
        edtAno = findViewById(R.id.edtAno);
        spnStatus = findViewById(R.id.spnStatus);
        checkBox = findViewById(R.id.favorito);
        lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LivroAdapter(listaLivros);
        lista.setAdapter(adapter);

        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvarLivro());
        carregarLivros();
    }

    private void carregarLivros() {
        db.collection("livros")
                .get()
                .addOnSuccessListener(query -> {
                    listaLivros.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Livro p = doc.toObject(Livro.class);
                        p.setId(doc.getId());
                        listaLivros.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
        adapter.setOnItemClickListener(livro -> {
            edtTitulo.setText(livro.getTitulo());
            edtAutor.setText(livro.getAutor());
            ArrayAdapter adapter = (ArrayAdapter) spnGenero.getAdapter();
            int posicao1 = adapter.getPosition(livro.getGenero());
            spnGenero.setSelection(posicao1);
            edtAno.setText(String.valueOf(livro.getAnoPubli()));
            ArrayAdapter adapter1 = (ArrayAdapter) spnStatus.getAdapter();
            int posicao2 = adapter1.getPosition(livro.getStatus());
            spnStatus.setSelection(posicao2);

            livroEditando = livro;
            ((Button) findViewById(R.id.btnSalvar)).setText("Atualizar Livro");
        });

    }

    private void limparCampos() {
        edtTitulo.setText("");
        edtAutor.setText("");
        spnGenero.setSelection(0);
        edtAno.setText("");
        spnStatus.setSelection(0);
        checkBox.setChecked(false);

        livroEditando = null;
        ((Button) findViewById(R.id.btnSalvar)).setText("Salvar Livro");
    }

    private void salvarLivro() {
        String titulo = edtTitulo.getText().toString();
        String autor = edtAutor.getText().toString();
        String genero = spnGenero.getSelectedItem().toString();
        String anoPubli = edtAno.getText().toString();
        String status = spnStatus.getSelectedItem().toString();
        boolean favorito = checkBox.isChecked();

        if (titulo.isEmpty() || autor.isEmpty() || genero.isEmpty() || anoPubli.isEmpty() || status.isEmpty()) {
            Toast.makeText(this, "Preencha os campos corretamente!", Toast.LENGTH_SHORT).show();
        }

        if (livroEditando == null) {
            Livro livro = new Livro(null, titulo, autor, genero, anoPubli, favorito, status);
            db.collection("livros")
                    .add(livro)
                    .addOnSuccessListener(doc -> {
                        livro.setId(doc.getId());
                        Toast.makeText(this, "Livro salvo!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarLivros();
                    });
        } else {
            livroEditando.setTitulo(titulo);
            livroEditando.setAutor(autor);
            livroEditando.setGenero(genero);
            livroEditando.setAnoPubli(anoPubli);
            livroEditando.setStatus(status);
            livroEditando.setFavorito(favorito);

            db.collection("livros").document(livroEditando.getId())
                    .set(livroEditando)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Livro atualizado!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarLivros();
                    });
        }
    }

    public void deletarLivro(android.content.Context context, String id) {
        new android.app.AlertDialog.Builder(context)
                .setTitle("Confirmar")
                .setMessage("Deseja deletar?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    db.collection("livros").document(id)
                            .delete()
                            .addOnSuccessListener(aVoid -> carregarLivros());
                })
                .setNegativeButton("Não", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    public void atualizarLivro(Livro livro) {
        db.collection("livros").document(livro.getId())
                .set(livro)
                .addOnSuccessListener(aVoid -> carregarLivros());
    }
}