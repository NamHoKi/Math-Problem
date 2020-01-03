import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class TextProofViewer extends JFrame implements ActionListener {
	JTextArea tarea = new JTextArea(3, 20);
	JTextField tf = new JTextField(5);
	JFileChooser fchooser;

	JButton btnSave, btnClear, btnCancel;
	JButton btn[] = new JButton[5];
	String blabel[] = {"Open",         "Save", "   ^  ", "   v   ", "  ?  "};
	JScrollPane jsp;
	JViewport jv;
	TextViewBoard tvas;
	int nSessions = 0;  //- to pay money? -
	int nStrokes = 0;
	Vector<HWChar> vc = new Vector<HWChar>();
	Vector<HWStroke> vs = new Vector<HWStroke>();
//	Vector<Symb> vsym = new Vector<Symb>();
	HWChar hwc;
//	HWChar hwme;
	int pen_from = -1, pen_to = -1; //- the ids of strokes, start-to-end, for a symbol -
	int t_sym = -1;	//- the token id in the ME label - token not correct, thus not useful -

	public static void main (String args[]) {
		new TextProofViewer(args);
	}
	String fn_aux = "";		// 2-19. 7. 25.
	TextProofViewer (String args[]) {
		super("TextProofViewer - 2019.5.4");
if (args.length > 0) { fn_aux = args[0];
	System.out.println("fn_aux = "+ fn_aux + " (for opting auxiliary sl_data files)");
}		JPanel p0 = new JPanel();
		getContentPane().add("North", p0);
		ImageIcon icon_logo = new ImageIcon("icon/proofviewer.png");
		p0.add(new JLabel(icon_logo));
		p0.add(new JLabel("   Info:"));
		JScrollPane spane = new JScrollPane(tarea);
		p0.add(spane);
		ImageIcon icon_new = new ImageIcon("icon/NEW.gif");
		ImageIcon icon_save = new ImageIcon("icon/SAVE.gif");
		ImageIcon icon_pick = new ImageIcon("icon/aima_pick_small.png");
		ImageIcon icon_up = new ImageIcon("icon/up-triangle-icon.png");
		ImageIcon icon_down = new ImageIcon("icon/down-triangle-icon.png");
		btn[0] = new JButton("", icon_new);
			p0.add(btn[0]); btn[0].addActionListener(this);
		for(int i = 1; i < 4; i++) {
			if (i == 1)		btn[i] = new JButton("++", icon_save);
			else if (i == 2)	btn[i] = new JButton("", icon_up);
			else if (i == 3)	btn[i] = new JButton("", icon_down);
			else	btn[i] = new JButton(blabel[4]);
			btn[i].addActionListener(this);
		}
		btn[1].setEnabled(false);
		p0.add(btn[1]);
		p0.add(new JLabel("Teach:")); p0.add(tf);	// TextField
		tf.addActionListener(this);
		p0.add(new JLabel(">", icon_pick, JLabel.RIGHT));		// Expand buttons
		JPanel p1 = new JPanel(new GridLayout(2,1));
			p1.add(btn[2]); p1.add(btn[3]);
			p0.add(p1); //p0.setBackground(Color.green);

		tvas = new TextViewBoard(this);
		//tvas.setSize(1000,700);
	//	tvas.setPreferredSize(new Dimension(700+100,600+200));
		tvas.setMinimumSize(new Dimension(800, 550));
		jsp = new JScrollPane(tvas, 
		        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, //AS_NEEDED
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//JPanel p2 = new JPanel(null); p2.add(tvas);
		jv = jsp.getViewport(); jv.setView(tvas);
		//jsp.add(p2);
		jv.setViewPosition(new Point(200,130));
	//	jsp.setRowHeader(new JViewport());
		getContentPane().add(jsp, "Center");
	//	setSize(800, 450);	

		FileNameExtensionFilter fxfilter = new FileNameExtensionFilter(
		"Text Files: txt, java", "txt");
		fchooser = new JFileChooser();
		try {
		File f = new File(new File("./datame").getCanonicalPath());
		fchooser.setCurrentDirectory(f);
		} catch(IOException ex) {}
		fchooser.setFileFilter(fxfilter);
	//	fchooser.addChoosableFileFilter(fxfilter);

		//getContentPane().add("Center", tvas);
		getContentPane().add("North", p0);
		setSize(800, 550/*335*/);
		setLocation(30,10);
		setVisible(true);
		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			close_symbol_label_f (); System.exit(1); }});
			//-- not useful ? ---//
		init();
	}

	void init() {
		String msg = "Welcome!\nLoad data -> pick a stroke group -> \ntune it with arrows -> Store. Good luck!\n";
		tarea.setText(msg);
	}
/*
class Symb {
	String label;
	int idz[] = {-1, -2};
	int bbx[];
	int pick_box[];
	Symb(String s) { label = s; }
	Symb(String s, int id1, int id2) { this(s); idz[0] = id1; idz[1] = id2; }
}
*/
	String fn, folder_name = "datame/";
	String fn_label, text_id;
	String hw_label;
	String Symz[];
	Vector<Symb> vtk = new Vector<Symb>();  //- stores ME label tokens - just for u-assistance
	Vector<Symb> vsym = new Vector<Symb>(); //- ME symbol segments' info - to be saved

	boolean load_handwriting_me() {
		System.out.println(" ** New Loader **");
	     String fn_test;
	     fn_test = "d://bkshin/Fujitsu/0.Course/AI/0실습자료/onwrite/datame/text_hw_00006A.txt";
	     boolean demo = true; demo = !demo;
	     int fc_result = 0;
	     if (!demo) fc_result = fchooser.showOpenDialog (tarea);
	     if (demo || fc_result == JFileChooser.APPROVE_OPTION) {  /* 0 */
		File file;
		if (!demo) { file = fchooser.getSelectedFile (); } 
		else { file = new File(fn_test); }
		fn = file.getName();
		folder_name /*fileParent*/ = file.getParent() + "/";
	//	int len = fn.length();
	//	fn_label = fn.substring(len-9, len-4);
	//	System.out.println("Data id: "+ fn_label +" (<- "+fn+")");
		int lab_start = fn.lastIndexOf("text_hw_") + 8;
		int lab_end = fn.lastIndexOf(".txt");
		fn_label = fn.substring(lab_start, lab_end);
		text_id = fn.substring(lab_start, lab_start+5);
	/**/	System.out.println("text_hw id: "+ fn_label +"/"+text_id);
		setTitle("TextProofViewer : "+ fn);

		hwc = new HWChar();	// member
		boolean state = hwc.load_handwriting(file, text_id /*fn_label*/);

		if (state) {
		     vc.addElement(hwc);
		     nStrokes = hwc.getSize();

	//	     int BBx[] = {0, 0, 0, 0}, bbx[];
		     for(int si = 0; si < hwc.getSize(); si++) {
		          HWStroke stroke = hwc.getStroke(si);
		          int nPs = stroke.getSize(); //hwc.getStroke(si).getSize();
	//	          bbx = stroke.getBounds();
		          if (si <= 5) {
			if (si == 0) tarea.append(nPs + ""); else tarea.append(", "+ nPs);
			if (si > 0) System.out.print(",");
			System.out.print(" " + nPs);
		          } else if (si < 15) {
			if (si == 6) { tarea.append(", "); System.out.print(", "); }
			tarea.append(".");
			System.out.print(".");
		          }
		     } System.out.println(" points each.");
		     tarea.append(" points");
		     if (nStrokes == 1)	tarea.append(".\n");
		     else		tarea.append(" each .\n");
		} else
		     System.out.println("Loading failed.\n");

		return state;

	     } else {
	          switch(fc_result) {
	          case JFileChooser.CANCEL_OPTION: /* 1*/
		          System.out.println("File Choose : cancelled."); break;
	          case JFileChooser.ERROR_OPTION: /* -1 */
		          System.out.println("File Choose : error."); break;
	          default: System.out.println("File Choose : unknown problem.");
	           }
	          return false;
	     }
	}

	boolean load_handwriting_segment() {
	      System.out.println(" ** Segments Loader **");
	      if (fn_label != null) {
	            try {
		String fname_sl = "text_sl_"+ fn_label + fn_aux + ".txt";
		System.out.println("Path: "+ folder_name); // folder name
		System.out.println("File: "+ fname_sl);
		BufferedReader sl_br;
		sl_br = new BufferedReader(new FileReader(folder_name + fname_sl));

		String  line, tk, tem; // input buffer
/*1*/		line = sl_br.readLine();	// text_id
		System.out.println("Text name: "+ line);
/*- now segments -*/
		int line_id = 0;
		while((line = sl_br.readLine()) != null) {
			//line = sl_br.readLine();
			line_id = line_id + 1;
			StringTokenizer st = new StringTokenizer(line);
			int n = st.countTokens();
			if (n < 3) {
			        System.out.println("Insufficient # tokens: line "+line_id +": " + 
				line + " -> ignored");
			        continue;
			}
			int i1, i2; String lab;
			i1 = Integer.parseInt(st.nextToken());
			i2 = Integer.parseInt(st.nextToken());
			lab = st.nextToken();
			Symb sym = new Symb(lab, i1, i2);
			vsym.add(sym);
		}
		sl_br.close();
	            } catch(IOException e) {
		System.out.println("File access error. Returning..."); System.out.println(e);
		return false;
	            }

	            System.out.println(", vsym.size = "+ vsym.size());
//	            for(int i = 0; i < vsym.size(); i++) { sym = vsym.elementAt(i);
//		System.out.println(sym.idz[0] +"  "+ sym.idz[1] +" <"+sym.label+">");
//	            }
	            return true;
	      } else return false;
	}

	BufferedWriter sl_bw;
	String fname_sl;
	boolean save_symbol_label_f () {
	      if (fn_label != null) {
	            try {
		if (sl_bw == null) { // if not opened yet.
		        System.out.println(" * save_symbol_label *");
	//	        System.out.println(" * fn_label = " + fn_label);
		        fname_sl = "text_sl_"+ fn_label +".txt";
		        System.out.println(" File: "+ fname_sl);
		        sl_bw = new BufferedWriter(new FileWriter(folder_name + fname_sl));
System.out.println(hwc.label);  // if a text, it could be very long.
		        sl_bw.write(hwc.label);
		        sl_bw.newLine();
		}
		String info_1 = sym.idz[0] + "  " + sym.idz[1] + "  " + sym.label;
			      // xmin xmax ymin ymax
		String info_r = "";
//		info_r = info_r + sym.bbx[0]+" "+sym.bbx[1]+" "+sym.bbx[2]+" "+sym.bbx[3];
		System.out.println("writing: " + info_1 + info_r);
		sl_bw.write(info_1);
		sl_bw.write(info_r);	// info_1 and info_r in a single line
		sl_bw.newLine();

	//      	sl_bw.close();
	            } catch(IOException e) { return false;}
	      }
	      return true;
	}
	void close_symbol_label_f () {		//---- not useful ? ----
	      try {
		if (sl_bw != null) {
		sl_bw.close();
		sl_bw = null;
	//	System.out.print(folder_name+"me_sl_"+fn_label+".txt / " + fname_sl);
	//	System.out.println(" - closed.");
		System.out.println(fname_sl +" - closed.");
		}
	      }catch (IOException ie) {}
	}

	void cancel_the_last () {
		if (vs.size() > 0)  vs.removeElementAt(vs.size() - 1);
		tvas.repaint();
	}
	void addStroke (HWStroke s) { vs.addElement(s); }

	int sym_from = -1,
	    sym_to = -1;
	boolean processStrokeSelection (Point p0) {
		boolean picked = false;

		HWChar hwchar = vc.elementAt(0);
		HWStroke s;
		vs = hwchar.vs;
		int x, y, xmin, xmax, ymin, ymax, pen_i = -1;
		int[] bx = makeSelBox(p0, 5);
		xmin = bx[0]; xmax = bx[1];
		ymin = bx[2]; ymax = bx[3];
	//	System.out.print("["+xmin+"-"+xmax+","+ymin+"-"+ymax+"] ");
		for(int i = 0; i < vs.size(); i++) {
			s = vs.elementAt(i); // System.out.print(".");
			Point[] pa = s.getPointArray();
			for(int j = 0; j < pa.length; j++) {
				x = pa[j].x; y = pa[j].y;
				if (x >= xmin && x <= xmax && y >= ymin && y <= ymax) {
					picked = true;
					pen_i = i;
					break;
			}	}
			if (picked) break;
		} //System.out.println("Stroke_"+ pen_i+" ? - prcessSS");
		if (pen_i != -1) { System.out.println("Stroke_"+ (pen_i+0) + " selected'");
			pen_from = pen_i; pen_to = pen_i;
		}
		return true;
	}
	int[] makeSelBox (Point p0, int rad) {
		int[] sbx =  new int[4];
		sbx[0] = p0.x - rad;
		sbx[1] = p0.x + rad;
		sbx[2] = p0.y - rad;
		sbx[3] = p0.y + rad;
		return sbx;
	}

	void resetSelection () {
		HWChar hwchar;
		for(int i = 0; i < vc.size(); i++) {
			hwchar = (HWChar) vc.elementAt(i);
			hwchar.setCold();
		}
	}
/*x*/	void report_RelationType(int rel_type) {
		tarea.append(" : relation = "+ (rel_type+1));
	}
	int getBrushSize() {
		int brush_size = 1;
		return brush_size;
	}

	void teach_starter() {
		t_sym = 0;
		pen_from = 0; pen_to = 0 - 1;
	//	if (t_sym < hwc.vtk.size())
	//		tf.setText(hwc.vtk.elementAt(t_sym).label);
		tvas.repaint();
	}
	void change_stroke_bound(int ender, int val) {
		if (hwc != null)
		if (ender == 0) { // start stroke -
			pen_from += val; if (pen_from < 0) pen_from -= val;
			if (pen_from > pen_to) pen_from = pen_to;
		} else {
			pen_to += val; if (pen_to >= nStrokes) pen_to -= val;
			if (pen_to < pen_from) pen_to = pen_from;
		}
	}

	public void actionPerformed (ActionEvent e) {
	        if (e.getSource() instanceof JButton) {
		JButton b = (JButton) e.getSource();
		if (b == btn[0]) {  // open/load
			Vector<HWChar> vc_new = new Vector<HWChar>();
			if (nSessions > 0) tarea.setText("");  // clear up, not at the start -

			close_symbol_label_f ();	// sl_bw = null;
			vc = new Vector<HWChar>();
			vsym = new Vector<Symb>();
String s = tf.getText();
fn_aux = s; if (!s.isEmpty()) System.out.println("fn_aux = " + s +" (sl file choice)");
			if (load_handwriting_me() == true)
			{	load_handwriting_segment();
				teach_starter();	// start teaching process :
				tarea.setCaretPosition(tarea.getText().length());

			int BBx[] = vc.elementAt(0).getBounds();
			jv.setViewPosition(new Point(Math.max(BBx[0]-30, 0),
					            Math.max(BBx[2]-30, 0)));
	//	System.out.println("* " + BBx[0]+","+BBx[1]+"-"+BBx[2]+","+BBx[3]);
	//	jv.setPreferredSize(new Dimension(BBx[1],BBx[3]));
		tvas.setPreferredSize(new Dimension(BBx[1]+20, BBx[3]+20));

			jv.repaint();
			}
			tvas.repaint();
			nSessions ++;
		} else if (b == btn[1]) {		//- save -
			save_symbol_segment();	// hwc.vsym.addElement(new Symb);
			save_symbol_label_f ();
			list_symbol_segment();
		} else if (b == btn[2]) {	//  ^ - expand the boundary back
			change_stroke_bound(0, -1);
			tvas.repaint();
		} else if (b == btn[3]) {	//  v - expand it forward
			change_stroke_bound(1, 1);
			tvas.repaint();
		}
	        } else if (e.getSource() instanceof JTextField) {  // <Enter>
		save_symbol_segment();
		save_symbol_label_f ();
		list_symbol_segment();
	        }
	}
	void list_symbol_segment() {
		for(int k = 0; k < hwc.vsym.size(); k++) {
			if (k <= 7)	System.out.print("<"+hwc.vsym.elementAt(k).label+"> ");
			else 	System.out.print(".");
		}
		System.out.println("");
	}

	Symb sym;  //- the latest sym -
	void save_symbol_segment() {
	        if (vc.size() <= 0) System.out.println("Empty handwriting.");
	        else {
		String lab2store = tf.getText();
		sym = new Symb(lab2store, pen_from, pen_to);
		hwc.vsym.addElement(sym);
		String out_seg = "Strokes "+ pen_from +"-"+ pen_to +" : "+lab2store;
		tarea.append(out_seg+"\n");
	//	System.out.println(out_seg); // + bbx, ...
		if (pen_from < nStrokes - 1) {
			pen_to++; pen_from = pen_to;  //- the following stroke -
		}
		t_sym++;
	/*	if (t_sym < hwc.vtk.size()) {  // convenience? not much - it's not exact
			tf.setText(hwc.vtk.elementAt(t_sym).label);
		}
	*/	tvas.repaint();
	        }
	}
}

/*class Symb {
	String label;
	int idz[] = {-1, -2};
	int bbx[];
	int pick_box[];
	Symb(String s) { label = s; }
	Symb(String s, int id1, int id2) { this(s); idz[0] = id1; idz[1] = id2; }
}*/


class TextViewBoard extends JPanel implements MouseListener, MouseMotionListener {
	TextProofViewer dtt;
	Vector<HWStroke> vs;
	HWStroke s;
	boolean inkdry;  // = false;
	int mode = 0;  // 1 = writing, 0 = pick/selection
	boolean tgl_enabled = false;

	TextViewBoard (TextProofViewer f) {
		dtt = f;
		addMouseListener(this);
		addMouseMotionListener(this);
		setBackground (new Color(235+20, 255, 215+39));
		vs = new Vector<HWStroke>();
	//	bomb = null;
	}
/*x*/	void toggle_mode () {
		if (tgl_enabled)	// fixed to pick/selection
		mode = (mode +1)% 2;
	}
Point bomb;
	public void mousePressed (MouseEvent e) {
		Point p = new Point(e.getX(), e.getY());
		int rtype = -1;

/*x*/		if ((rtype = pickRelation(p.x, p.y)) >= 0) {
			rel_type = rtype;
		} else
			dtt.resetSelection();

		s = new HWStroke(dtt.getBrushSize());		/* start a new stroke */
		s.addPoint(p);
		dtt.addStroke(s);
		vs.addElement(s);
bomb = p;
		inkdry = false;
		repaint();
	}

	public void mouseReleased (MouseEvent e) {
		Point p = new Point(e.getX(), e.getY());
		s.addPoint(p);
		if (mode == 0) {  // 0 = pick/selection, cf. 1 = writing
			int rtype = -1;
bomb = null;
/* ok */			dtt.cancel_the_last();
		//		dtt.processStrokeSelection(p);
			vs.removeElementAt(vs.size() - 1);
			dry_ink();
		}
	}
	public void mouseClicked (MouseEvent e) {}
	public void mouseEntered (MouseEvent e) {}
	public void mouseExited (MouseEvent e) {}
	public void mouseDragged (MouseEvent e) {
		Point p = new Point(e.getX(), e.getY());
		if (mode == 0) {
			int rtype= -1;
			if ((rtype = pickRelation(p.x, p.y)) >= 0) {
			    System.out.print(rtype);
			    if (rtype != rel_type) {
				rel_type = rtype;
			}    }
	bomb = p;
		} else {
			s.addPoint(p);
		}
		repaint();
	}
	public void mouseMoved (MouseEvent e) {}
	HWStroke getStroke () { return s; }

	public void paint (Graphics g) {
		super.paint(g);
		g.setColor(new Color(220, 220, 255));
		int ww = getWidth();
		for(int i = 0; i < 5; i++) g.drawLine(10, 80+70*i, ww-10, 80+70*i);

		g.setColor(Color.black);
		if (dtt.hw_label != null) { int x = 20, y = 30, dcw = 5, spw = 5;
		        for(int i = 0; i < dtt.hwc.vtk.size(); i++) {
			Symb sym = dtt.hwc.vtk.elementAt(i);
			g.drawString(sym.label, x, y);  // symbol
			x += dcw*sym.label.length() + spw;
		}       }
//		g.setColor(new Color(255, 210, 210));
		if (dtt.vc.size() > 0) {
		      Vector<HWStroke> vs = dtt.vc.elementAt(0).vs;
		      draw_bbxz_bombed(g, dtt.vsym, vs);

		      for(int i = 0; i < vs.size(); i ++) { HWStroke hws = vs.elementAt(i);
			if (dtt.pen_from <= i && i <= dtt.pen_to 
			    || !is_symbol_labeled(i, dtt.vsym)) {	//- symbol label missing
				g.setColor(Color.red);
			  Graphics2D g2 = (Graphics2D) g;
			  g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, 
					 BasicStroke.JOIN_ROUND));
				hws.draw2(g2, 2);
			} else {	g.setColor(Color.black);
				hws.draw(g);
			}
		      }
		}
		if (inkdry == false) {
			HWStroke s;
			if (mode == 1)
				g.setColor(Color.blue);  // wet drawing
			else	g.setColor(Color.red);  // editing
			for(int i = 0; i < vs.size(); i ++) {
			      s = (HWStroke) vs.elementAt(i);
			      if (mode == 1)
				      s.draw2(g, 1);  // wet drawing
			      else	      s.draw(g);    // editing
			      if (mode == 0) {
				int[] sbx = s.getBounds();
				if (sbx[1]-sbx[0] < 10 && sbx[3]-sbx[2] < 10) {
				    Point p = (Point) s.vp.lastElement();
				    drawMarker(g, p);  // pick focus
				}
			      }
			}
		}
	}
	boolean is_symbol_labeled(int s, Vector<Symb> vsym) {
		for(int i = 0; i < vsym.size(); i++) {
			if (is_contained(s, vsym.elementAt(i))) return true;
		}
		return false;
	}
	boolean is_contained(int s, Symb symb) {
		if (s >= symb.idz[0] && s <= symb.idz[1]) return true;
		else return false;
	}

	void draw_bbxz_bombed(Graphics g, Vector<Symb> vsym, Vector<HWStroke> vs) {
		g.setColor(Color.green);
		int symbx[], bbx[];
		double xploder = 0, yploder = 0;
		for(int s = 0; s < vsym.size(); s++) {
			int fto[] = vsym.elementAt(s).idz;
			symbx = vs.elementAt(fto[0]).getBounds();
	//		System.out.print("<"+vsym.elementAt(s).label+"> : "+fto[0]);
			for(int j = fto[0]+1; j <= fto[1]; j++) {
	//			System.out.print(" "+j);
				bbx = vs.elementAt(j).getBounds();
				if (bbx[0] < symbx[0]) symbx[0] = bbx[0];
				if (bbx[1] > symbx[1]) symbx[1] = bbx[1];
				if (bbx[2] < symbx[2]) symbx[2] = bbx[2];
				if (bbx[3] > symbx[3]) symbx[3] = bbx[3];
			}
 // System.out.println(" ("+ (fto[1]-fto[0]+1)+") ["+symbx[0]+"-"+symbx[1]+","+symbx[2]+"-"+symbx[3]+"]");
			xploder = 0; yploder = 0;
			if (bomb != null) {
				xploder = symbx[0] - bomb.x;
				yploder = symbx[2] - bomb.y;
				double dist = Math.sqrt(xploder*xploder + yploder*yploder);
				if (dist < 50) { double ang = Math.atan2(yploder, xploder);
					
					xploder = (50 - dist)*Math.cos(ang);
					yploder = (50 - dist)*Math.sin(ang);
//if (s <= 1) 
//System.out.printf("%d. Explode dist = %.1f @(%.1f, %.1f), ang=%.1f -> xxplode!(%d, %d)\n",
//	 s, dist, xploder, yploder, ang*180/3.14156, (int)xploder, (int)yploder);
					g.setColor(new Color(255, 128, 128));
		g.drawLine(symbx[0], symbx[2], symbx[0]+(int)xploder, symbx[2]+(int)yploder);
		g.setColor(Color.green);
				} else { xploder = 0; yploder = 0; }
			}
		g.drawRect(symbx[0], symbx[2], symbx[1]-symbx[0], symbx[3]-symbx[2]);
			g.setColor(Color.blue);
		g.drawString(vsym.elementAt(s).label, symbx[0]+(int)xploder, symbx[2]+(int)yploder);
			g.setColor(Color.green);
		}
	}



	void draw_bbxz(Graphics g, Vector<Symb> vsym, Vector<HWStroke> vs) {
		g.setColor(Color.green);
		int symbx[], bbx[];
		for(int s = 0; s < vsym.size(); s++) {
			int fto[] = vsym.elementAt(s).idz;
			symbx = vs.elementAt(fto[0]).getBounds();
	//		System.out.print("<"+vsym.elementAt(s).label+"> : "+fto[0]);
			for(int j = fto[0]+1; j <= fto[1]; j++) {
	//			System.out.print(" "+j);
				bbx = vs.elementAt(j).getBounds();
				if (bbx[0] < symbx[0]) symbx[0] = bbx[0];
				if (bbx[1] > symbx[1]) symbx[1] = bbx[1];
				if (bbx[2] < symbx[2]) symbx[2] = bbx[2];
				if (bbx[3] > symbx[3]) symbx[3] = bbx[3];
			} // System.out.println(" ("+ (fto[1]-fto[0]+1)+") ["+symbx[0]+"-"+symbx[1]+","+symbx[2]+"-"+symbx[3]+"]");
			g.drawRect(symbx[0], symbx[2], symbx[1]-symbx[0], symbx[3]-symbx[2]);
			g.setColor(Color.blue);
			g.drawString(vsym.elementAt(s).label, symbx[0], symbx[2]);
			g.setColor(Color.green);
		}
	}

	boolean isClose(int Ar[], int x, int y) {
		int x1=Ar[0], x2=Ar[2], y1=Ar[1], y2=Ar[3];
		boolean logicx, logicy;
		if (x1 <= x2) logicx = x >= x1-5 && x <= x2+5;
		else  logicx = x >= x2-5 && x <= x1+5;
		if (y1 <= y2) logicy = y >= y1-5 && y <= y2+5;
		else  logicy = y >= y2-5 && y <= y1+5;
		return logicx && logicy;
	}
	int pickRelation(int x, int y) {
/*x*/		if (ARel_exists) {
			double dists[] = new double[Arow.length];
			double r_min;
			for(int k = 0; k < Arow.length; k++) {
				dists[k] = line_point_distance(k,x,y);
			}
			int k_min = -1;
			r_min = 1e6; //dists[0];
			for(int k = 0; k < Arow.length; k++)
			    if (isClose(Arow[k], x, y))
			       if (dists[k] < r_min) { k_min = k; r_min = dists[k]; }
			if (false && k_min >= 0) {
			    System.out.println(": LineSeg ("+Arow[k_min][0]+", "+
			        Arow[k_min][1]+")->("+Arow[k_min][2]+", "+Arow[k_min][3]+")");
			    tpr_lp_dist(k_min, x,  y);
			}
			if (r_min < 5) return k_min;
		}
		return -1;
	}
/*x*/	double line_point_distance(int k, int x, int y) { // how close it is to the k-th arrow?
		// y = s*x + y1 - s*x1, where s = (y2-y1)/(x2-x1)
		// a*x + by + c = 0, where a = dy, b = -dx, c = (y1*dx - x1*dy), dx = x2-x1.
		// dist = abs(ax + by + c) / sqrt(a^a+b^b)
		double dx = Arow[k][2] - Arow[k][0], dy = Arow[k][3] - Arow[k][1];
		double a = dy, b = -dx, c = Arow[k][1]*dx - Arow[k][0]*dy;
		double norm = Math.sqrt(a*a+b*b);
		a /= norm; b /= norm; c /= norm;
		double d = Math.abs(a*x + b*y + c); // / Math.sqrt(a*a+b*b);
		return d;
	}
/*x*/	double tpr_lp_dist(int k, int x, int y) { // how close it is to the k-th arrow?
		// y = s*x + y1 - s*x1, where s = (y2-y1)/(x2-x1)
		// a*x + by + c = 0, where a = dy, b = -dx, c = (y1*dx - x1*dy), dx = x2-x1.
		// dist = abs(ax + by + c) / sqrt(a^a+b^b)
		double dx = Arow[k][2] - Arow[k][0], dy = Arow[k][3] - Arow[k][1];
		double a = dy, b = -dx, c = Arow[k][1]*dx - Arow[k][0]*dy;
		double norm = Math.sqrt(a*a+b*b);
		a /= norm; b /= norm; c /= norm;
		double d = Math.abs(a*x + b*y + c); // / Math.sqrt(a*a+b*b);
		System.out.println("Arow_"+k+": ["+Arow[k][0]+","+Arow[k][1]+", "+
			Arow[k][2]+","+Arow[k][3]+"] <-> xy("+x+", "+y+"): d = "+ (int)d);
		System.out.printf("a= %.2f, b= %.2f, c= %.2f -> d= %.2f/1\n",
			a, b, c, Math.abs(a*x + b*y + c));
		return d;
	}

	public void drawMarker (Graphics g, Point p) {
		int s = 2, d = 2;
		g.fillRect(p.x-d -s, p.y-d -s, s+s, s+s);
		g.fillRect(p.x+d,     p.y-d -s, s+s, s+s);
		g.fillRect(p.x+d,     p.y+d,     s+s, s+s);
		g.fillRect(p.x-d -s, p.y+d,     s+s, s+s);
	}
	public void dry_ink () {
		inkdry = true;
		repaint();
	}
	public void cancel_writing () {
		vs = new Vector<HWStroke>();
		inkdry = true;
	}
	int Arow[][] = new int[8][4];
	int rscale = -1, sscale = -1;
/*x*/	boolean ARel_exists = false;
	int rel_type = -1;

}
