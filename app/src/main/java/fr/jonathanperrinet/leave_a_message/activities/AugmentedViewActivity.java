package fr.jonathanperrinet.leave_a_message.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.List;

import fr.jonathanperrinet.leave_a_message.Rajawali.MessagesRenderer;
import fr.jonathanperrinet.leave_a_message.leave_a_message.R;

/**
 * Created by Jonathan Perrinet.
 */
public class AugmentedViewActivity extends AppCompatActivity implements MessagesRenderer.RendererListener {

    private static final String TAG = "Activity3D";
    MessagesRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented_view);

        final RajawaliSurfaceView surface = new RajawaliSurfaceView(this);
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);

        // Add mSurface to your root view
        addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));

        renderer = new MessagesRenderer(this);
        surface.setSurfaceRenderer(renderer);
    }

    @Override
    public List<String> getMessages() {
        List<String> messages = new ArrayList<>();

        Intent intent = getIntent();
        if(intent != null) {
            String svg = intent.getStringExtra("svg");
            messages.add(svg);
        }

        return messages;
    }
}
